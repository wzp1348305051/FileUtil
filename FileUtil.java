import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;

import org.apache.http.util.ByteArrayBuffer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;

public class FileUtil {
	private static final String TAG = "FileUtil";
	
	/**
	 * 创建文件，若文件夹不存在则自动创建文件夹，若文件存在则删除旧文件
	 * @param path :待创建文件路径
	 * */
	public static File createNewFile(String path) {
		File file = new File(path);
		try {
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			if (file.exists()) {
				file.delete();
			}
			file.createNewFile();
		} catch (IOException e) {
			LogUtil.e(TAG, e.getMessage());
		}
		return file;
	}
	
	/**
	 * 将文件输入流写入文件
	 * */
	public static boolean writeFileFromInputStream(InputStream inStream, String path) {
		boolean result = true;
		try {
			File file = createNewFile(path);
			FileOutputStream out = new FileOutputStream(file);
			byte[] data = new byte[1024];
			int num = 0;
			while ((num = inStream.read(data, 0, data.length)) != -1) {
				out.write(data, 0, num);
			}
			out.close();
			data = null;
		} catch (Exception e) {
			result = false;
			LogUtil.e(TAG, e.getMessage());
		}
		return result;
	}
	
	/**
	 * 获取文件输入流
	 * */
	public static InputStream readFileToInputStream(String path) {
		InputStream inputStream = null;
		try {
			File file = new File(path);
			inputStream = new FileInputStream(file);
		} catch (IOException e) {
			LogUtil.e(TAG, e.getMessage());
		}
		return inputStream;
	}
	
	/**
	 * 读取文件字节数组
	 * */
	public static byte[] readFileToBytes(String path) {
		InputStream inputStream = readFileToInputStream(path);
		if (inputStream != null) {
			byte[] data = new byte[1024];
			ByteArrayBuffer buffer = new ByteArrayBuffer(1024);
			int n = 0;
			try {
				while ((n = inputStream.read(data)) != -1) {
					buffer.append(data, 0, n);
				}
				data = null;
				inputStream.close();
			} catch (IOException e) {
				LogUtil.e(TAG, e.getMessage());
			}
			return buffer.toByteArray();
		}
		return null;
	}
	
	/**
	 * 读取文件内容
	 * */
	public static String readFileToString(String path) {
		byte[] dataBytes = readFileToBytes(path);
		if (dataBytes != null) {
			return new String(dataBytes);
		}
		return null;
	}
	
	/**
	 * 以行为单位读取文件，常用于读面向行的格式化文件
	 */
	public static String readFileByLines(String path) {
		File file = new File(path);
		if (!file.exists() || file.isDirectory()) {
			return null;
		}
		StringBuffer buffer = new StringBuffer();
		FileReader fileReader = null;
		BufferedReader bufferedReader = null;
		try {
			fileReader = new FileReader(file);
			bufferedReader = new BufferedReader(fileReader);
			String tempString = null;
			while ((tempString = bufferedReader.readLine()) != null) {
				buffer.append(tempString).append(System.getProperty("line.separator"));
			}
		} catch (Exception e) {
			LogUtil.e(TAG, e.getMessage());
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (Exception e) {
					LogUtil.e(TAG, e.getMessage());
				}
			}
			if (fileReader != null) {
				try {
					fileReader.close();
				} catch (Exception e) {
					LogUtil.e(TAG, e.getMessage());
				}
			}
		}
		return buffer.toString();
	}
	
	/**
	 * 根据给出路径自动选择复制文件或整个文件夹
	 * @param src :源文件或文件夹路径
	 * @param dest :目标文件或文件夹路径
	 * */
	public static void copyFiles(String src, String dest) {
		File srcFile = new File(src);
		if (srcFile.exists()) {
			if (srcFile.isFile()) {
				writeFileFromInputStream(readFileToInputStream(src), dest);
			} else {
				File[] subFiles = srcFile.listFiles();
				if (subFiles.length == 0) {
					File subDir = new File(dest);
					subDir.mkdirs();
				} else {
					for (File subFile : subFiles) {
						String subDirPath = dest + System.getProperty("file.separator") + subFile.getName();
						copyFiles(subFile.getAbsolutePath(), subDirPath);
					}
				}
			}
		}
	}
	
	/**
	 * 根据给出路径自动选择删除文件或整个文件夹
	 * @param path :文件或文件夹路径
	 * */
	public static void deleteFiles(String path) {
		File file = new File(path);
		if (!file.exists()) {
			return;
		}
		if (file.isFile()) {
			file.delete();// 删除文件
		} else {
			File[] subFiles = file.listFiles();
			for (File subfile : subFiles) {
				deleteFiles(subfile.getAbsolutePath());// 删除当前目录下的子目录
			}
			file.delete();// 删除当前目录
		}
	}
	
	/**
	 * 将图片保存为文件
	 * */
	public static void saveBitmapToFile(Bitmap bitmap, String path) {
		if (null == bitmap || null == path || 0 == path.trim().length()) {
			return;
		}
		File file = createNewFile(path);
		try {
			FileOutputStream out = new FileOutputStream(file);
			bitmap.compress(CompressFormat.JPEG, 85, out);
			out.flush();
			out.close();
		} catch (IOException e) {
			LogUtil.e(TAG, e.getMessage());
		}
	}
	
	/**
	 * 根据文件地址解析bitmap，OOM时对bitmap进行压缩输出(压缩至1/4或者1/16)
	 * @param path :文件地址
	 */
	public static Bitmap getBitmapFromFile(String path) {
		Bitmap bitmap = null;
		if (null == path || 0 == path.trim().length()) {
			return bitmap;
		}
		File file = new File(path);
		if (file.exists()) {
			try {
				bitmap = BitmapFactory.decodeFile(path, null);
			} catch (OutOfMemoryError e) {
				BitmapFactory.Options options = new BitmapFactory.Options();
				try {
					System.gc();
					options.inSampleSize = 2;
					bitmap = BitmapFactory.decodeFile(path, options);
				} catch (OutOfMemoryError e1) {
					try {
						System.gc();
						options.inSampleSize = 4;
						bitmap = BitmapFactory.decodeFile(path, options);
					} catch (OutOfMemoryError e2) {
						System.gc();
						LogUtil.e(TAG, e2.getMessage());
					}
				}
			}
		}
		return bitmap;
	}
		
	/**
	 * 转换文件大小
	 * */
	public static String FormetFileSize(long size) {//转换文件大小
		DecimalFormat df = new DecimalFormat("#.00");
		String result = "";
		if (size < 1024) {
			result = df.format((double) size) + "B";
		} else if (size < 1048576) {
			result = df.format((double) size / 1024) + "K";
		} else if (size < 1073741824) {
			result = df.format((double) size / 1048576) + "M";
		} else {
			result = df.format((double) size / 1073741824) + "G";
		}
		return result;
	}
	
}
