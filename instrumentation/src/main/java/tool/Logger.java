package tool;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Logger {

	private static String FILENAME = "default.txt";

	public Logger() {
	}

	public Logger(String file_name) {

		this.FILENAME = file_name + ".txt";
	}

	public static void writeLine(String line) {

		BufferedWriter bw = null;

		try {

			bw = new BufferedWriter(new FileWriter(FILENAME, true));
			bw.write(line);
			bw.newLine();

		} catch (IOException e) {

			e.printStackTrace();

		} finally {

			try {

				if (bw != null)
					bw.close();

			} catch (IOException ex) {

				ex.printStackTrace();

			}

		}

	}

}
