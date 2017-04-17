

public class StatsBranch {

  	String class_name_;
  	String method_name_;
  	int pc_;
  	int taken_;
  	int not_taken_;

    public StatsBranch(String class_name, String method_name, int pc){

          class_name_ = class_name;
    			method_name_ = method_name;
    			pc_ = pc;
    			taken_ = 0;
    			not_taken_ = 0;
}

    public void print(Logger logger) {

          logger.writeLine(class_name_ + '\t' + method_name_ + '\t' + pc_ + '\t' + taken_ + '\t' + not_taken_);
    }

    public void incrTaken(){
    			taken_++;
    }

    public void incrNotTaken(){
    			not_taken_++;
    }


}
