package pt.tecnico.cnv.common;

/**
 * Created by miguel on 12/05/17.
 */
public class STATIC_VALUES {


    //----------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------
    //----------------------------- DEBUG ----------------------------------------------
    //----------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------
    //variable that allows to shutdown all debug
    public final static boolean DEBUG = true;
    //----------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------
    public final static boolean DEBUG_HTTP_REQUEST = DEBUG && false;
    public final static boolean DEBUG_LOAD_BALANCER_CHOSEN_MACHINE = DEBUG && false;
    public final static boolean DEBUG_LOAD_BALANCER_WEB_SERVER_READY = DEBUG && true;
    public final static boolean DEBUG_LOAD_BALANCER_JOB_ALREADY_INSTRUCTED = DEBUG && false;
    public final static boolean DEBUG_LOAD_BALANCER_JOB_UPDATE = DEBUG && false;


    //----------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------


    public final static String SEPARATOR_STORAGE_METRIC_REQUEST = ":!:";

    public final static int NUMBER_SECONDS_INTERVAL_WEB_SERVER_CHECKS_METRIC = 5;
    public final static int NUMBER_MILI_SECONDS_INTERVAL_WEB_SERVER_CHECKS_METRIC = NUMBER_SECONDS_INTERVAL_WEB_SERVER_CHECKS_METRIC*1000;

    public final static long LOWER_THRESHOLD = 0;
    public final static long UPPER_THRESHOLD = 0;

    public final static String AMI_ID = "ami-48b2a62c";
    public final static int NUMBER_SECONDS_INTERVAL_LOAD_BALANCER_CHECKS_SPEED_WORKERS = 5;
    public final static int NUMBER_MILI_SECONDS_INTERVAL_LOAD_BALANCER_CHECKS_SPEED_WORKERS = NUMBER_SECONDS_INTERVAL_LOAD_BALANCER_CHECKS_SPEED_WORKERS*1000;
    public final static int NUMBER_MINUTES_SPEED_WINDOW = 10;
    public final static int NUMBER_SECONDS_SPEED_WINDOW = NUMBER_MINUTES_SPEED_WINDOW*60;
    public final static int NUMBER_ELEMENTS_QUEUE_SPEEDS = NUMBER_SECONDS_SPEED_WINDOW / NUMBER_SECONDS_INTERVAL_LOAD_BALANCER_CHECKS_SPEED_WORKERS ;
}
