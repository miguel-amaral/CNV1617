package tool;


import BIT.highBIT.*;

import java.io.File;
import java.io.StringWriter;
import java.io.PrintWriter;

import java.util.Enumeration;
import java.util.Vector;

import tool.Logger;
import tool.StatsBranch;

public class InstTool{

	private static int dyn_method_count = 0;
	private static int dyn_bb_count = 0;
	private static int dyn_instr_count = 0;

	private static int newcount = 0;
	private static int newarraycount = 0;
	private static int anewarraycount = 0;
	private static int multianewarraycount = 0;

	private static int loadcount = 0;
	private static int storecount = 0;
	private static int fieldloadcount = 0;
	private static int fieldstorecount = 0;

	private static StatsBranch[] branch_info;
	private static int branch_number;
	private static int branch_pc;
	private static String branch_class_name;
	private static String branch_method_name;

	private static Logger logger;


	//#############################################//
	//#############################################//
	//---------------UTIL FUNCTIONS----------------//
	//#############################################//
	//#############################################//


	public static void printError(String error){

			logger.writeLine("##################################################");
			logger.writeLine("Syntax: java InstTool -stat_type in_path out_path");
			logger.writeLine("        in_path:  directory from which the class files are read");
			logger.writeLine("        out_path: directory to which the class files are written");
			logger.writeLine("Error found: " + error);
			logger.writeLine("##################################################");
			System.exit(-1);

}


public static synchronized void printDynamic(String foo){

			logger.writeLine("##################################################");
			logger.writeLine("Dynamic information summary of the class: " + foo);
			logger.writeLine("Number of methods:      " + dyn_method_count);
			logger.writeLine("Number of basic blocks: " + dyn_bb_count);
			logger.writeLine("Number of instructions: " + dyn_instr_count);
			logger.writeLine("##################################################");

}


public static synchronized void printBranch(String foo){


			logger.writeLine("##################################################");
			logger.writeLine("Branch summary:");
			logger.writeLine("CLASS NAME" + '\t' + "METHOD" + '\t' + "PC" + '\t' + "TAKEN" + '\t' + "NOT_TAKEN");
			logger.writeLine("##################################################");

	    for (int i = 0; i < branch_info.length; i++) {
	      if (branch_info[i] != null) {
	        branch_info[i].print(logger);
	      }
	    }
}

	//############################################################//
	//############################################################//
	//---------------DYNAMIC PROCESSMENT FUNCTIONS----------------//
	//############################################################//
	//############################################################//

public static synchronized void dynInstrCount(int incr){
				dyn_instr_count += incr;
				dyn_bb_count++;
	}

public static synchronized void dynMethodCount(int incr){
				dyn_method_count++;
	}


public static synchronized void setBranchClassName(String name){
	    branch_class_name = name;
	}

public static synchronized void setBranchMethodName(String name){
	    branch_method_name = name;
	}

public static synchronized void setBranchPC(int pc){
	    branch_pc = pc;
	}

public static synchronized void branchInit(int n){
	    if (branch_info == null) {
	      branch_info = new StatsBranch[n];
	    }
	}

public static synchronized void updateBranchNumber(int n){
	    branch_number = n;

	    if (branch_info[branch_number] == null) {
	      branch_info[branch_number] = new StatsBranch(branch_class_name, branch_method_name, branch_pc);
	    }
	}

public static synchronized void updateBranchOutcome(int br_outcome){
	    if (br_outcome == 0) {
	      branch_info[branch_number].incrNotTaken();
	    }
	    else {
	      branch_info[branch_number].incrTaken();
	    }
	}



public static void processBasicDynInfo(String in_filename, String out_filename){


				ClassInfo ci = new ClassInfo(in_filename);

				for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {

					Routine routine = (Routine) e.nextElement();
					routine.addBefore("tool.InstTool", "dynMethodCount", new Integer(1));

					for (Enumeration b = routine.getBasicBlocks().elements(); b.hasMoreElements(); ) {
						BasicBlock bb = (BasicBlock) b.nextElement();
						bb.addBefore("tool.InstTool", "dynInstrCount", new Integer(bb.size()));
					}
				}

				ci.addAfter("tool.InstTool", "printDynamic", in_filename);
				ci.write(out_filename);

}

public static void processBasicBranchInfo(String in_filename, int total){


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

public static void processOutcomeBranchInfo(String in_filename, String out_filename, int total, int k){


				ClassInfo ci = new ClassInfo(in_filename);

				for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {

						Routine routine = (Routine) e.nextElement();
						routine.addBefore("tool.InstTool", "setBranchMethodName", routine.getMethodName());
						InstructionArray instructions = routine.getInstructionArray();

					for (Enumeration b = routine.getBasicBlocks().elements(); b.hasMoreElements(); ) {

							BasicBlock bb = (BasicBlock) b.nextElement();
							Instruction instr = (Instruction) instructions.elementAt(bb.getEndAddress());
							short instr_type = InstructionTable.InstructionTypeTable[instr.getOpcode()];

							if (instr_type == InstructionTable.CONDITIONAL_INSTRUCTION) {

									instr.addBefore("tool.InstTool", "setBranchPC", new Integer(instr.getOffset()));
									instr.addBefore("tool.InstTool", "updateBranchNumber", new Integer(k));
									instr.addBefore("tool.InstTool", "updateBranchOutcome", "BranchOutcome");
									k++;
							}
					}
				}

				ci.addBefore("tool.InstTool", "setBranchClassName", ci.getClassName());
				ci.addBefore("tool.InstTool", "branchInit", new Integer(total));
				ci.addAfter("tool.InstTool", "printBranch", "null");
				ci.write(out_filename);
}



public static void doDynamic(File in_dir, File out_dir){

					String filelist[] = in_dir.list();
					int k = 0;
					int total = 0;

					for (int i = 0; i < filelist.length; i++) {

							String filename = filelist[i];
							if (filename.endsWith(".class")) {





									String in_filename = in_dir.getAbsolutePath() + System.getProperty("file.separator") + filename;
									String out_filename = out_dir.getAbsolutePath() + System.getProperty("file.separator") + filename;


									System.out.println(in_filename + "\n " + out_filename );

									processBasicDynInfo(in_filename, out_filename);
									processBasicBranchInfo(in_filename, total);

									processOutcomeBranchInfo(in_filename, out_filename, total, k);



							}
				 }
}





//############################################################//
//############################################################//
//----------------------------MAIN----------------------------//
//############################################################//
//############################################################//


public static void main(String argv[]){

			logger = new Logger("test");





			if (argv.length < 2 || argv.length > 2) {
					printError(" # of arguments < 2 || # of arguments > 2 ");
			}

			try {

					File in_file = new File(argv[0]);
					File out_dir = new File(argv[1]);

					System.out.println("Checking is they're directories ..\n");

					if (in_file.isDirectory() && out_dir.isDirectory()) {

						doDynamic(in_file, out_dir);
					}

					else {

						printError("in_path / out_path must be a directory");
					}
			}

			catch (NullPointerException e) {

				StringWriter error = new StringWriter();
				e.printStackTrace(new PrintWriter(error));

				printError(error.toString());

			}



}

}
