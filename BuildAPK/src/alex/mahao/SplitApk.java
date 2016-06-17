package alex.mahao;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SplitApk {
	HashMap<String, String> qudao = new HashMap<String, String>();// 渠道号，渠道名

	ArrayList<String> channelList = new ArrayList<>();

	String basePath;// 当前文件夹路径

	// apk 名
	private String apkName;

	// 秘钥文件名
	private String keystoreName ;

	//仓库密码
	private String storepass ;

	// 口令密码
	private String keypass ;

	// 口令名
	private String keyName ;

	// 签名工具地址（全路径）
	private String jarsignerPath ;

	public SplitApk(String apkName, String keystoreName, String storepass,
			String keyName, String keypass, String jarsignerPath) {
		this.apkName = apkName;
		this.keystoreName = keystoreName;
		this.storepass = storepass;
		this.keypass = keypass;
		this.keyName = keyName;
		this.jarsignerPath = jarsignerPath;

		this.basePath = new File("").getAbsolutePath();
	}

	public void mySplit() {
		getCannelFile();// 获得自定义的渠道号

		modifyChannel(); // 开始打包

	}

	/**
	 * 修改渠道字段
	 */
	public void modifyChannel() {

		// 1， 将该App 反编译
		String cmdUnpack = "cmd.exe /C java -jar apktool.jar d -f -s "
				+ apkName;
		runCmd(cmdUnpack);

		System.out.println("*********反编译Apk 成功 ***********");

		// 2, 移动清单文件，作为备份

		// 获取编译后后的目录名 和目录文件
		String decodeDir = apkName.split(".apk")[0];
		
		// 
		File decodeDirFile = new File(decodeDir);

		// 获取清单文件
		String maniPath = decodeDirFile.getAbsolutePath()
				+ "\\AndroidManifest.xml";

		// 获取备份清单文件目录 工程根目录
		String maniPathSave = basePath + "\\AndroidManifest_back.xml";

		// 备份清单文件
		new File(maniPath).renameTo(new File(maniPathSave));
		System.out.println("*********备份清单文件 ***********");

		for (int i = 0; i < channelList.size(); i++) {
			
			System.out.println("*********开始搞----"+channelList.get(i)+" ***********");
			// 获取备份文件的内容，修改渠道值，并保存到maniPath 中
			updateChannel(maniPathSave, maniPath, channelList.get(i));
			System.out.println("*********修改清单文件，替换清单文件成功 ***********");

			// 重新打包
			String cmdPack = String.format(
					"cmd.exe /C java -jar apktool.jar b %s %s", decodeDir,
					apkName);

			runCmd(cmdPack);

			System.out.println("*********4,打包成功，开始重新签名 ***********");

			// 签名文件地址
			String keyStorePath = basePath + "\\" + keystoreName;

			// 未签名的apk 地址
			String unsign_apk_path = decodeDir + "\\dist\\" + apkName;

			// 签名后的apk
			String sign_apk_path = basePath + "\\" + channelList.get(i)
					+ ".apk";

			String signCmd = jarsignerPath
					+ " -digestalg SHA1 -sigalg MD5withRSA -verbose -keystore "
					+ keyStorePath + " -storepass " + storepass + " -keypass "
					+ keypass + " -signedjar " + sign_apk_path + " "
					+ unsign_apk_path + " " + keyName;

			runCmd(signCmd);

			System.out.println("*********5," + channelList.get(i)
					+ "签名成功***********");
		}
	}

	/**
	 * 修改渠道值
	 * 
	 * @param sourcePath
	 *            备份清单文件地址
	 * @param targetPath
	 *            目标清单文件地址
	 * @param channelStr
	 *            要求该的渠道值
	 */
	public void updateChannel(String sourcePath, String targetPath,
			String channelStr) {

		BufferedReader br = null;
		FileReader fr = null;
		FileWriter fw = null;
		try {
			// 从备份中读取内容
			fr = new FileReader(sourcePath);
			br = new BufferedReader(fr);
			String line = null;
			StringBuffer sb = new StringBuffer();
			while ((line = br.readLine()) != null) {
				// 如果某一行存在qwertyy 则替换该值
				if (line.contains("qwertyy")) {
					line = line.replaceAll("qwertyy", channelStr);
				}
				sb.append(line + "\n");
			}
			// 写到目标清单文件
			fw = new FileWriter(targetPath);
			fw.write(sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (fr != null) {
					fr.close();
				}
				if (br != null) {
					br.close();
				}
				if (fw != null) {
					fw.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * 获取渠道字段
	 */
	private void getCannelFile() {
		File file = new File("channel.txt");
		// 如果文件不存在，则提示
		if (file.exists() && file.isFile()) {
			BufferedReader br = null;
			FileReader fr = null;
			try {

				// 获取到渠道的输入流
				br = new BufferedReader(new FileReader(file));

				String line = null;

				while ((line = br.readLine()) != null) {
					// 获取到渠道
					channelList.add(line.trim());
				}

				System.out.println(channelList);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (fr != null) {
						fr.close();
					}
					if (br != null) {
						br.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			System.out.println("*********获取渠道成功 ***********");
		} else {
			System.err
					.println("*********error: channel.txt文件不存在，请添加渠道文件***********");
		}
	}

	/**
	 * 执行控制台指令
	 * 
	 * @param cmd
	 */
	public void runCmd(String cmd) {
		Runtime rt = Runtime.getRuntime();
		BufferedReader br = null;
		InputStreamReader isr = null;
		try {
			// 执行
			Process p = rt.exec(cmd);
			// 获取对应流，一遍打印控制台输出的信息
			isr = new InputStreamReader(p.getInputStream());
			br = new BufferedReader(isr);
			String msg = null;
			while ((msg = br.readLine()) != null) {
				System.out.println(msg);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (isr != null) {
					isr.close();
				}
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
