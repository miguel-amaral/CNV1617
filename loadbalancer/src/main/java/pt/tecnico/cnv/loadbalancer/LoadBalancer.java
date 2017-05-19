package pt.tecnico.cnv.loadbalancer;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.Instance;
import pt.tecnico.cnv.common.HttpAnswer;
import pt.tecnico.cnv.common.HttpRequest;
import pt.tecnico.cnv.common.STATIC_VALUES;

import java.util.*;

/**
 * Created by miguel on 10/05/17.
 */
public class LoadBalancer extends TimerTask {

    private AmazonEC2 ec2;
    private InstanceLauncher instanceLauncher;
    private int _counterOfBootingInstances = 0;
    private List<Instance> _known_instances = new ArrayList<Instance>();
    private List<String> _instances_set_to_removal = new ArrayList<String>();
    private Map<String,Long> _metricsProccessedSinceLastTick = new HashMap<String, Long>();
    private final Map<String, Container> _instances = new HashMap<String, Container>();

    private Map<String,JobContainer> _jobs = new HashMap<String, JobContainer>();
    private Timer timer;
    private Map<String, Long> _snapshots = new HashMap<String, Long>();
    private List<String> _wentToZeroInstances = new ArrayList<String>();



    //whenever a new instance pops up a new key is created
    private final Map<String,EvictingQueueContainer> _speeds = new HashMap<String, EvictingQueueContainer>();

    public LoadBalancer(AmazonEC2 ec2)  {
        this.ec2 = ec2;
        this.instanceLauncher = new InstanceLauncher(ec2);
        updateInstances();
        startService();
    }

    private void startService() {
        timer = new Timer();
        timer.scheduleAtFixedRate(this, STATIC_VALUES.NUMBER_MILI_SECONDS_INTERVAL_LOAD_BALANCER_CHECKS_SPEED_WORKERS, STATIC_VALUES.NUMBER_MILI_SECONDS_INTERVAL_LOAD_BALANCER_CHECKS_SPEED_WORKERS);
    }

    private Map<String,Long> makeSnapshotInstancesMetrics(){
        Map<String,Long> snapshot = new HashMap<String, Long>();
        //create base for next
        synchronized (_instances){
            for (Map.Entry<String, Container> entry : _instances.entrySet()) {
                String id = entry.getKey();
                long metric = entry.getValue().metric;
                snapshot.put(id,metric);
            }
        }
        return snapshot;
    }


    // syncronize _metricsProccessedSinceLastTick then _speeds
    public void run() {
        System.out.println("Another " + STATIC_VALUES.NUMBER_MILI_SECONDS_INTERVAL_LOAD_BALANCER_CHECKS_SPEED_WORKERS+ " have gone by");
        synchronized (_metricsProccessedSinceLastTick) {
            //Register new Speed
//            System.out.println("Got lock");
            for(Map.Entry<String, Long> entry : _snapshots.entrySet()) {
//                System.out.println("1 for");
                String id = entry.getKey();
//                System.out.println("2 for");
                if (_wentToZeroInstances.contains(id)) {
                   System.out.println("Went to zero, ignoring");
                   addRegisteredSpeed(id,null);
//                   System.out.println("Speed registered");
                   continue;
                }
                Long processed = _metricsProccessedSinceLastTick.get(entry.getKey());
//                System.out.println("2");
                if(processed == null) {processed = 0L;}
//                System.out.println("3");
                long metric = processed;
//                System.out.println("4");
                Long baselineObject = entry.getValue();
//                System.out.println("5");
                if(baselineObject == null) {
                    System.out.println("Baseline was null, ignoring");
                    addRegisteredSpeed(id,null);
//                    System.out.println("Registered");
                    continue;
                }
//                System.out.println("6");
                long baseline = baselineObject;
//                System.out.println("7");
                if( baseline == 0 ) {
                    //need to ignore, we dont know if it was sleeping
                    System.out.println("Did not had enough at start");
                    addRegisteredSpeed(id,null);
//                    System.out.println("registered");
                    continue;
                }

                //add speed to list
                synchronized (_speeds) {
//                    System.out.println("got lock speeds");
                    addRegisteredSpeed(entry.getKey(), metric);
//                    System.out.println("registered");
                }
            }
            //Reset the list
//            System.out.println("reseting");
            _wentToZeroInstances = new ArrayList<String>();
//            System.out.println("zeros");
            _metricsProccessedSinceLastTick = new HashMap<String, Long>();
//            System.out.println("all");
        }
//        System.out.println("pre snapshot");
        _snapshots = makeSnapshotInstancesMetrics();
        System.out.println("Exiting run()");
    }

    private void addRegisteredSpeed(String key, Long metric) {
//        System.out.println("addRegisteredSpeed");
        synchronized (_speeds) {
//            System.out.println("got lock addRegisteredSpeed");
            EvictingQueueContainer queue = _speeds.get(key);
//            System.out.println("got queue");
            queue.addElement(metric);
//            System.out.println("element added");
        }
    }

    private void increaseMetric(Instance instance, long metricValue) {
        synchronized (_instances) {
            _instances.get(instance.getInstanceId()).metric = _instances.get(instance.getInstanceId()).metric + metricValue;
        }
    }
    private void decreaseMetric(Instance instance, long metricValue) {
        synchronized (_instances) {
            _instances.get(instance.getInstanceId()).metric = _instances.get(instance.getInstanceId()).metric - metricValue;
        }
    }
    private long getMetric(Instance instance) {
        synchronized (_instances) {
            return _instances.get(instance.getInstanceId()).metric;
        }
    }

    public void launchInstance(int numberOfInstances) {
        instanceLauncher.launchNewInstance(numberOfInstances);
    }

    public void terminateInstance(String instanceId) {
        instanceLauncher.destroyInstances(instanceId);
    }

    public HttpAnswer processQuery(String query, String letter) {
        while(true) {
            updateInstances();
            Instance lowestInstance = getLightestMachine();

            GetMetricValue requester = new GetMetricValue(query);

            long metricValue = requester.getMetric();
            boolean alreadyInstrumented = requester.isAlreadyIntrumented();
            boolean guess = !alreadyInstrumented;
            this.increaseMetric(lowestInstance, metricValue);
            String jobID = newJobID();
            synchronized (_jobs) {
                _jobs.put(jobID, new JobContainer(lowestInstance, metricValue,guess));
            }
            String ip = lowestInstance.getPublicIpAddress();
            if (STATIC_VALUES.DEBUG_LOAD_BALANCER_CHOSEN_MACHINE) {
                System.out.println("ip: " + ip + " chosen for " + jobID);
            }

            //String letter = alreadyInstrumented ? "alreadyInstrumented" : "r" ;
            if (STATIC_VALUES.DEBUG_LOAD_BALANCER_JOB_ALREADY_INSTRUCTED) {
                System.out.println("jobID: " + jobID + " already instrument: " + alreadyInstrumented);
            }
            HttpAnswer answer = HttpRequest.sendGet(ip + ":8000/" + letter + "?" + query + "&jobID=" + jobID, new HashMap<String, String>());
            if(answer.status() != 200) {
                synchronized(_jobs) {
                    _jobs.remove(jobID);
                }
                continue;
            } else {
                return answer;
            }
        }
    }

    private synchronized String newJobID(){
        return UUID.randomUUID().toString();
    }

    private void updateInstances() {
        List<Instance> instances = new ListWorkerInstances(ec2).listInstances();
        synchronized (_instances) {
            List<String> keys = new ArrayList<String>(_instances.keySet());
            for (Instance instance : instances) {
                String id = instance.getInstanceId();
                if (!_instances.containsKey(id)) {
                    this.newInstance(instance);
                } else {
                    keys.remove(id);
                }
            }
            for (String zombieId : keys) {
                System.out.println("We have " + zombieId + " down :(");
                _instances.remove(zombieId);
            }
        }
    }

    private void newInstance(Instance instance) {
        String id = instance.getInstanceId();
        if(_instances_set_to_removal.contains(id)) {
            //it is shutting down ..
            return;
        }
        if(this.instanceIsReady(instance)){
            synchronized (_speeds) {
                _speeds.put(id, new EvictingQueueContainer());
            }
            synchronized (_instances) {
                _instances.put(id,new Container(instance,0));
            }
        }
    }

    private boolean instanceIsReady(Instance instance) {
        String publicIpAddress = instance.getPublicIpAddress();
        HttpAnswer answer = HttpRequest.sendGet(publicIpAddress+":8000/ping", new HashMap<String, String>());
        if(STATIC_VALUES.DEBUG_LOAD_BALANCER_WEB_SERVER_READY) {
            System.out.println("instance: " + publicIpAddress + " is ready: " + (answer.status() == 200));
        }
        return answer.status() == 200;
    }

    public long getAvgSpeedAll(){
        long avg_sum = 0;
        int avg_valid = 0;
        synchronized (_speeds) {
            for (Map.Entry<String, EvictingQueueContainer> entry : _speeds.entrySet()) {
                long avg_current = entry.getValue().speed;
                if(avg_current != -1) {
                    avg_sum += entry.getValue().speed;
                    avg_valid++;
                }
            }
        }
        return avg_sum / avg_valid;
    }
    public long getLastSpeedAll(){
        long last_sum = 0;
        int last_valid = 0;
        synchronized (_speeds) {
            for (Map.Entry<String, EvictingQueueContainer> entry : _speeds.entrySet()) {
                long last_current = entry.getValue().lastInserted();
                if(last_current != -1) {
                    last_sum += entry.getValue().speed;
                    last_valid++;
                }
            }
        }
        return last_sum / last_valid;
    }

    public String toString() {

        String newLine = "\n";
        StringBuilder toReturn = new StringBuilder("Lower threshold:" + STATIC_VALUES.LOWER_THRESHOLD+ newLine);
        toReturn.append("Upper threshold:" + STATIC_VALUES.UPPER_THRESHOLD).append(newLine).append(newLine).append(newLine).append(newLine);
        toReturn.append("Last    average: ").append(getLastSpeedAll()).append(newLine);
        toReturn.append("Average average: ").append(getAvgSpeedAll()).append(newLine);

        toReturn.append("avg : last : Instances:").append(newLine);
        synchronized (_instances) {
            for (Map.Entry<String, Container> entry : _instances.entrySet()) {
                String instanceID = entry.getKey();
                Long avgSpeed = getAvgSpeed(instanceID);
                Long lastSpeed = getLastSpeed(instanceID);
                toReturn.append(avgSpeed).append(" : ").append(lastSpeed).append(" : ").append(instanceID).append(" : ").append(entry.getValue().metric).append(" time: ").append(entry.getValue().updateMinutes()).append(newLine);
                toReturn.append(instanceRecordedSpeeds(instanceID)).append(newLine).append(newLine);
            }
        }
        toReturn.append(newLine).append("Removed:").append(newLine);
        for (String id : _instances_set_to_removal) {
            toReturn.append(id).append(newLine);
        }
        toReturn.append(newLine);
        toReturn.append(newLine).append("Current Jobs:");
        synchronized (_jobs) {
            toReturn.append(" ").append(_jobs.size()).append(newLine);
            for (Map.Entry<String, JobContainer> entry : _jobs.entrySet()) {
                toReturn.append(entry.getKey()).append(" : ").append(entry.getValue()).append(newLine);
            }
        }
        return toReturn.toString();
    }

    private String instanceRecordedSpeeds(String instanceID) {
        synchronized (_speeds){
            return _speeds.get(instanceID).printSpeeds();
        }
    }

    private Long getLastSpeed(String instanceID) {
        synchronized (_speeds){
            return _speeds.get(instanceID).lastInserted();
        }
    }

    private Long getAvgSpeed(String instanceID) {
        synchronized (_speeds){
            return _speeds.get(instanceID).speed;
        }
    }

    public Instance getLightestMachine() {
        long min = Long.MAX_VALUE; //max int

        List<Instance> minInstance = null;
        synchronized (_instances) {
            for (Map.Entry<String, Container> entry : _instances.entrySet()) {
                if (entry.getValue().metric < min || minInstance == null) {
                    minInstance = new ArrayList<Instance>();
                    minInstance.add(entry.getValue().instance);
                    min = entry.getValue().metric;
                } else if (entry.getValue().metric == min) {
                    minInstance.add(entry.getValue().instance);
                }
            }
        }
        int index = 0;
        int size = minInstance.size();
        if(size > 1) {
            Random rn = new Random();
            index = rn.nextInt(size);
        }
        return minInstance.get(index);
    }

    public void jobDone(String jobId) {
        JobContainer job;
        long missingMetric ;
        synchronized(_jobs) {
            job = _jobs.get(jobId);
            missingMetric = job.missingMetric();
            this.decreaseMetric(job.instance, missingMetric);
            _jobs.remove(jobId);
        }
        processUpdate(job,missingMetric);

    }

    private void processUpdate(JobContainer job, long processsedMetric)  {
        String instance_id = job.instance.getInstanceId();
        long current_metric = getMetric(job.instance);
        synchronized (_metricsProccessedSinceLastTick) {
            if (current_metric == 0) {
                _wentToZeroInstances.add(instance_id);
            }
            Long alreadyProcessed = _metricsProccessedSinceLastTick.get(instance_id);
            if(alreadyProcessed == null) { alreadyProcessed = 0L; }
            _metricsProccessedSinceLastTick.put(instance_id,alreadyProcessed+processsedMetric);
        }
    }

    public void updateJob(String jobID, long metric) {
        JobContainer job;
        synchronized (_jobs) {
            job = _jobs.get(jobID);
        }

        long difference = job.getMetricDifference(metric);
        job.passed_metric = metric;
        if(difference != 0) { decreaseMetric(job.instance,difference); }
        processUpdate(job,metric);
    }

    class Container {
        Instance instance;
        long metric;
        Date launchTime;
        int minutes;
        Container(Instance instance, long metric) {
            this.instance = instance;
            this.metric = metric;
            launchTime = instance.getLaunchTime();
        }

        int updateMinutes(){
            Date dt2 = new Date();
            long diff = dt2.getTime() - launchTime.getTime();
            this.minutes = (int) (diff / (60 * 1000) % 60);
            return this.minutes;
        }
    }

    class JobContainer {
        Instance instance;
        boolean guess;
        long final_metric;
        long passed_metric = 0;

        JobContainer(Instance instance, long metric, boolean guess) {
            this.instance = instance;
            this.final_metric = metric;
            this.guess = guess;
            this.passed_metric = 0;
        }

        long getMetricDifference(long metric) {
            //dont overflow..
            long difference = metric-passed_metric;
            if(!guess) {return difference;}

            long jobMissingMetric = final_metric - passed_metric;
            if(difference > jobMissingMetric) {
                return 0;
            } else {
                return difference;
            }
        }

        long getRawMetricDifference(long metric) {
            return metric-passed_metric;
        }

        public long missingMetric() {
            return final_metric-passed_metric;
        }

        @Override
        public String toString() {
            return instance.getPublicIpAddress() + (guess? " GUESS " : "") + " " + passed_metric + " out of " + final_metric;
        }
    }

    class EvictingQueueContainer {
        private int numberInserted;
        private ArrayDeque<Long> queue;
        private int numberValid;
        private long current_sum;
        private long speed;

        EvictingQueueContainer() {
            queue = new ArrayDeque<Long>(STATIC_VALUES.NUMBER_ELEMENTS_QUEUE_SPEEDS);
            numberInserted = 0;
            numberValid = 0;
            current_sum = 0;
            speed = -1;
        }

        void addElement(Long metric) {
            if(metric == null) metric = -1L;
//            System.out.println("add element");
            if(numberInserted == STATIC_VALUES.NUMBER_ELEMENTS_QUEUE_SPEEDS) {
//                System.out.println("first equal");
                Long last = queue.removeFirst();
//                System.out.println("removed");
                if(last != -1L) {
                    numberValid--;
                    current_sum -= metric;
                }
            } else {
//                System.out.println("else");
                numberInserted++;
            }
            if(metric != -1L) {
//                System.out.println("metric not null");
                current_sum += metric;
                numberValid++;
            }
//            System.out.println("add last");
            queue.addLast(metric);
//            System.out.println("just added");
            if(numberValid == 0) {
//                System.out.println("not valid");
                speed = -1;
            } else {
//                System.out.println("number valid");
                speed = current_sum / numberValid;
            }
            System.out.println(speed+"");
//            System.out.println("exiting addElement");
        }

        Long lastInserted() {
            return queue.peekLast();
        }

        public String printSpeeds() {
            StringBuilder toReturn = new StringBuilder();
            for(Long speed : queue) {
                toReturn.append(speed).append(" : ");
            }
            return toReturn.toString();
        }
    }
}
