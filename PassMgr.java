import java.io.*;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.Scanner;

public class PassMgr {

	public static void main(String[] args) throws FileNotFoundException {

		String passFile = null;
		String flag = null;
		File file;

		passFile = args[1];
		flag = args[3];

		if ((!args[0].equals("-file")) || (!args[2].equals("-flag"))) {
			System.out
					.print("Invalid syntax, invoke with: -file <pass_file> -flag <I|N|C>");
		} else {
			char f = flag.charAt(0);
			file = new File(passFile);
			router(f, file);
		}
	}

	private static void router(char f, File file) throws FileNotFoundException {
		switch (f) {
		case 'I':
			createFile(file);
			break;
		case 'N':
			addUser(file);
			break;
		case 'C':
			changePassword(file);
			break;
		default:
			System.out.print("Invalid flag, must use <I|N|C>");
			break;
		}
	}

	private static void createFile(File file) throws FileNotFoundException {

		FileOutputStream pass = new FileOutputStream(file);

	}

	private static int generateSalt() {
		int salt = (0 + (int) (Math.random() * Integer.MAX_VALUE));
		return salt;
	}

	private static void addUser(File file) throws FileNotFoundException {

		String user, password;
		int salt;

		user = checkUser(file);
		password = checkPassword(file);

		salt = generateSalt();
		String hash = generateHash(salt, password);
		String write = user + ":" + salt + ":" + hash;
		writeToFile(write, file);
		System.out.println("User successfully added");
		
	}
	
	private static String checkUser(File file) throws FileNotFoundException {
	
		String user;
		Scanner scan = new Scanner(System.in);
		Scanner readFile = new Scanner(file);
		
		System.out.print("Enter username: ");
		user = scan.next();
		
		while (readFile.hasNextLine()) {
			String lineFromFile = readFile.nextLine();
			String[] split = lineFromFile.split(":");
			
			if (split[0].equals(user)) {
				System.out.println("Username already exists");
				checkUser(file);
			}
		}
		
		if (user.length() > 8) {
			System.out.println("Username must be no greater than 8 characters");
			checkUser(file);
		}
		
		return user;
	}

	private static String checkPassword(File file) {

		String password, confirm;
		Scanner scan = new Scanner(System.in);

		System.out.print("Enter password: ");
		password = scan.next();

		if (password.length() < 8 || password.length() > 16) {
			System.out.println("Password must be between 8 and 16 characters.");
			System.exit(0);
		}

		System.out.print("Confirm password: ");
		confirm = scan.next();

		if (!confirm.equals(password)) {
			System.out.print("Passwords must be identical");
			checkPassword(file);
		} else if (confirm.length() < 16) {
			//System.out.println("Padding password to 16 characters");
			for (int i = confirm.length(); i < 16; i++) {
				confirm += Integer.toBinaryString(0);
			}
			//System.out.print(confirm);
		}
		scan.close();
		return confirm;
	}

	private static void changePassword(File file) throws FileNotFoundException {

		Scanner scan = new Scanner(System.in);
		Scanner readFile = new Scanner(file);

		String user, curPass, curSalt, newPass, conPass;
		int newSalt;

		System.out.print("Enter your username: ");
		user = scan.next();

		while (readFile.hasNextLine()) {

			String lineFromFile = readFile.nextLine();
			String[] split = lineFromFile.split(":");

			if (split[0].equals(user)) {

				System.out.println("User " + user + " found...");
				System.out.print("Enter your current password: ");
				curPass = scan.next();

				if (curPass.length() < 16) {
					//System.out.println("Padding password to 16 characters");
					for (int i = curPass.length(); i < 16; i++) {
						curPass += Integer.toBinaryString(0);
					}
					//System.out.println(curPass);
				}

				curSalt = split[1];
				int curSaltInt = Integer.parseInt(curSalt);

				String curPassHash = generateHash(curSaltInt, curPass);

				if (curPassHash.equals(split[2])) {

					System.out.println("Password match...");
					System.out.println("New password prompt:");
					newPass = checkPassword(file);
					newSalt = generateSalt();
					String newPassHash = generateHash(newSalt, newPass);
					String write = user + ":" + newSalt + ":" + newPassHash;
					rewriteLine(write, file, user);
					System.out.println("Password changed successfully");
					
				} else {
					System.out
							.println("Password does not match stored password");
				}

				break;
			} else {
				System.out.println("Username not found, exiting program...");
				System.exit(0);
			}
		}
	}

	private static String generateHash(int salt, String pass) {

		String hash = "";
		String passbytes = "";

		try {

			byte[] bytes = ByteBuffer.allocate(4).putInt(salt).array();

			byte firstbyte = bytes[0];
			byte secondbyte = bytes[1];
			byte secondLastByte = bytes[2];
			byte lastByte = bytes[3];

			MessageDigest md = MessageDigest.getInstance("SHA-512");
			byte[] digestpass = md.digest(pass.getBytes());
			for (int i = 0; i < digestpass.length - 1; i++) {
				passbytes = passbytes + digestpass[i] + "";
			}
			hash = firstbyte + "" + secondbyte + "" + passbytes + ""
					+ secondLastByte + "" + lastByte;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return hash;
	}

	private static void writeToFile(String write, File file) {
		try {
			PrintWriter out = new PrintWriter(new PrintWriter(new FileWriter(
					file, true)));
			out.println(write);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void rewriteLine(String toWrite, File file, String userName) {
		try {
	
			File temp = new File("tempFile.txt");

			BufferedReader reader = new BufferedReader(new FileReader(file));
			BufferedWriter writer = new BufferedWriter(new FileWriter(temp));

			String currentLine;

			while ((currentLine = reader.readLine()) != null) {
				if (currentLine.contains(userName)) {
					writer.write(toWrite);
				} else {
					writer.write(currentLine);
				}
				writer.newLine();
			}
			reader.close();
			writer.close();
			temp.renameTo(file);
		} catch (Exception e) {
			e.printStackTrace();

		}
	}
}