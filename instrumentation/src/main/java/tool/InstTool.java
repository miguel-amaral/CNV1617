package tool;


import BIT.highBIT.*;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Enumeration;



public class InstTool {

    private static int dyn_method_count = 0;
    private static int dyn_bb_count = 0;
    private static int dyn_instr_count = 0;

//    private static int newcount = 0;
//    private static int newarraycount = 0;
//    private static int anewarraycount = 0;
//    private static int multianewarraycount = 0;
//
//    private static int loadcount = 0;
//    private static int storecount = 0;
//    private static int fieldloadcount = 0;
//    private static int fieldstorecount = 0;

    private static StatsBranch[] branch_info;
    private static int branch_number;
    private static int branch_pc;
    private static String branch_class_name;
    private static String branch_method_name;

    private static Logger logger;

    private static int k = 0;
    private static int total = 0;


    //#############################################//
    //#############################################//
    //---------------UTIL FUNCTIONS----------------//
    //#############################################//
    //#############################################//


    public static void printError(String error) {

        logger.writeLine("##################################################");
        logger.writeLine("Syntax: java InstTool -stat_type in_path out_path");
        logger.writeLine("        in_path:  directory from which the class files are read");
        logger.writeLine("        out_path: directory to which the class files are written");
        logger.writeLine("Error found: " + error);
        logger.writeLine("##################################################");
        System.exit(-1);

    }


    public static synchronized void printDynamic(String foo) {


        logger.writeLine("##################################################");
        logger.writeLine("Dynamic information summary of the class: " + foo);
        logger.writeLine("Number of methods:      " + dyn_method_count);
        logger.writeLine("Number of basic blocks: " + dyn_bb_count);
        logger.writeLine("Number of instructions: " + dyn_instr_count);


    }


    public static synchronized void printBranch(String foo) {

        if (branch_info.length == 0) return;

        logger.writeLine("Branch summary:");
        logger.writeLine("CLASS NAME" + '\t' + "METHOD" + '\t' + "PC" + '\t' + "TAKEN" + '\t' + "NOT_TAKEN");


        for (int i = 0; i < branch_info.length; i++) {
            if (branch_info[i] != null) {
                branch_info[i].print(logger);
            }
        }
        logger.writeLine("##################################################");
    }

    //############################################################//
    //############################################################//
    //---------------DYNAMIC PROCESSMENT FUNCTIONS----------------//
    //############################################################//
    //############################################################//

    public static boolean canProcess(String filename) {

        if (filename.endsWith(".class"))

            if (filename.startsWith("Main.") || filename.startsWith("RayTracer.") || filename.startsWith("Vector.") || filename.startsWith("Matrix.")) {

                System.out.println(filename);
                return true;
            }

        return false;

    }


    public static synchronized void dynInstrCount(int incr) {
        DataContainer data = ContainerManager.getInstance(Thread.currentThread().getId());
//        data.instructions += incr;
        data.bb_blocks++;
    }

    public static synchronized void dynMethodCount(int incr) {
        DataContainer data = ContainerManager.getInstance(Thread.currentThread().getId());
        data.methods++;

    }


    public static synchronized void setBranchClassName(String name) {

        branch_class_name = name;
    }

    public static synchronized void setBranchMethodName(String name) {

        branch_method_name = name;
    }

    public static synchronized void setBranchPC(int pc) {

        branch_pc = pc;
    }

    public static synchronized void branchInit(int n) {

        if (branch_info == null)
            branch_info = new StatsBranch[n];

    }


    public static synchronized void updateBranchNumber(int n) {

        branch_number = n;

        if (branch_info[branch_number] == null) {
            branch_info[branch_number] = new StatsBranch(branch_class_name, branch_method_name, branch_pc);
        }
    }

    public static synchronized void updateBranchOutcome(int br_outcome) {


        DataContainer data = ContainerManager.getInstance(Thread.currentThread().getId());


        if (br_outcome == 0) {
            data.branch_fail++;
        } /*else {
            data.branch_success++;
        }*/
    }



    public static void doBranch(File in_dir) {

        String filelist[] = in_dir.list();

        int k = 0;
        int total = 0;

        for (int i = 0; i < filelist.length; i++) {

            String filename = filelist[i];

            if (canProcess(filename)) {

                String in_filename = in_dir.getAbsolutePath() + System.getProperty("file.separator") + filename;
                ClassInfo ci = new ClassInfo(in_filename);

                for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {

                    Routine routine = (Routine) e.nextElement();


                    InstructionArray instructions = routine.getInstructionArray();

                    for (Enumeration b = routine.getBasicBlocks().elements(); b.hasMoreElements(); ) {

                        BasicBlock bb = (BasicBlock) b.nextElement();


                        Instruction instr = (Instruction) instructions.elementAt(bb.getEndAddress());
                        short instr_type = InstructionTable.InstructionTypeTable[instr.getOpcode()];

                        if (instr_type == InstructionTable.CONDITIONAL_INSTRUCTION) {
                            total++;
                        }
                    }
                }

            }
        }

        for (int i = 0; i < filelist.length; i++) {

            String filename = filelist[i];

            if (canProcess(filename)) {

                String in_filename = in_dir.getAbsolutePath() + System.getProperty("file.separator") + filename;

                ClassInfo ci = new ClassInfo(in_filename);

                for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {

                    Routine routine = (Routine) e.nextElement();

                    routine.addBefore("tool/InstTool", "dynMethodCount", new Integer(1));
                    routine.addBefore("tool/InstTool", "setBranchMethodName", routine.getMethodName());

                    InstructionArray instructions = routine.getInstructionArray();

                    for (Enumeration b = routine.getBasicBlocks().elements(); b.hasMoreElements(); ) {

                        BasicBlock bb = (BasicBlock) b.nextElement();
                        bb.addBefore("tool/InstTool", "dynInstrCount", new Integer(bb.size()));

                        Instruction instr = (Instruction) instructions.elementAt(bb.getEndAddress());
                        short instr_type = InstructionTable.InstructionTypeTable[instr.getOpcode()];

                        if (instr_type == InstructionTable.CONDITIONAL_INSTRUCTION) {

                            instr.addBefore("tool/InstTool", "setBranchPC", new Integer(instr.getOffset()));
                            instr.addBefore("tool/InstTool", "updateBranchNumber", new Integer(k));
                            instr.addBefore("tool/InstTool", "updateBranchOutcome", "BranchOutcome");
                            k++;
                        }
                    }
                }

                ci.addBefore("tool/InstTool", "setBranchClassName", ci.getClassName());
                ci.addBefore("tool/InstTool", "branchInit", new Integer(total));

                ci.write(in_filename);
            }
        }

    }


//############################################################//
//############################################################//
//----------------------------MAIN----------------------------//
//############################################################//
//############################################################//


    public static void main(String argv[]) {

        logger = new Logger("test");


        if (argv.length < 2 || argv.length > 2) {
            printError(" # of arguments < 2 || # of arguments > 2 ");
        }
        System.out.println("Instrumenting in dir: " + argv[0]);
        System.out.println("Instrumenting output to dir: " + argv[0]);
        try {

            File in_dir = new File(argv[0]);


            if (in_dir.isDirectory()) {

                doBranch(in_dir);
            } else {

                printError("in_path / out_path must be a directory");
            }
        } catch (NullPointerException e) {

            StringWriter error = new StringWriter();
            e.printStackTrace(new PrintWriter(error));

            printError(error.toString());

        }


    }

}