package alex.mahao;


public class Main {

	public static void main(String[] args) {// 这里用cmd传入参数用

		// apk的名字
		String apkName = "test.apk";
		
		//签名文件
		String keystoreName = "alex.keystore";
		
		// 初始密码
		String storepass = "123456";
		
		// 名字
		String keyName = "test";
		
		// 密码
		String keypass = "111111";
		
		
		
		// 打包的地址 去jdk 中找
		String jarsignerPath = "C:\\Program Files\\Java\\jdk1.8.0_91\\bin\\jarsigner.exe";

		
		// 开始打包
		new SplitApk(apkName,keystoreName,storepass,keyName,keypass,jarsignerPath).mySplit();
	
		
	}

}
