/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zq.lyrics.formats.zrce;


import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okio.BufferedSource;
import okio.Okio;

/**
 * 表示一首歌的歌词对象,它可以以某种方式来画自己
 *
 * @author hadeslee
 */
public class Lyric implements Serializable {

	private static final long serialVersionUID = 20071125L;
	private long tempTime;// 表示一个暂时的时间,用于拖动的时候,确定应该到哪了
	List<Sentence> list = new ArrayList<Sentence>();// 里面装的是所有的句子
	private boolean initDone;// 是否初始化完毕了
	private transient File file;// 该歌词所存在文件
	private boolean enabled = true;// 是否起用了该对象,默认是起用的
	private int offset;// 整首歌的偏移量
	// 用于缓存的一个正则表达式对象
	private static final Pattern pattern = Pattern
			.compile("(?<=\\[).*?(?=\\])");
	private String songname;
	/**
	 * 读取某个指定的歌词文件,这个构造函数一般用于 拖放歌词文件到歌词窗口时调用的,拖放以后,两个自动关联
	 *
	 *            歌曲信息
	 */
	public Lyric(String lrcPath, String songname) {
		this.songname = songname;
		this.file = new File(lrcPath);
		if(!file.exists()){
			initDone = false;
			return;
		}
		init(file);
		initDone = true;
	}

	/**
	 * 设置此歌词是否起用了,否则就不动了
	 *
	 * @param b
	 *            是否起用
	 */
	public void setEnabled(boolean b) {
		this.enabled = b;
	}

	/**
	 * 得到此歌词保存的地方
	 *
	 * @return 文件
	 */
	public File getLyricFile() {
		return file;
	}

	public static void closeQuietly(Closeable closeable) {

		if (closeable != null) {

			try {

				closeable.close();

			} catch (RuntimeException rethrown) {

				throw rethrown;

			} catch (Exception ignored) {

			}

		}

	}

	/**
	 * 调整整体的时间,比如歌词统一快多少 或者歌词统一慢多少,为正说明要快,为负说明要慢
	 *
	 * @param time
	 *            要调的时间,单位是毫秒
	 */
	public void adjustTime(int time) {
		// 如果是只有一个显示的,那就说明没有什么效对的意义了,直接返回
		if (list.size() == 1) {
			return;
		}
		offset += time;
	}

	public String decodeLrc(File file) {
		BufferedSource bufferedSource = null;
		byte[] bytes;
		try {
			bufferedSource = Okio.buffer(Okio.source(file));
			bytes = bufferedSource.readByteArray();
			byte keys[] = {(byte) 0xCE, (byte) 0xD3, 'n', 'i', '@', 'Z', 'a', 'w', '^', '2', 't', 'G', 'Q', '6', (byte) 0xA5, (byte) 0xBC};

			for(int i = 0; i < bytes.length; i++)
			{
				byte ccc = bytes[i];
				byte f =  (byte) (ccc ^ keys[i % 16]);
				bytes[i] = f;
			}

			return new String(bytes,"UTF-8");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			closeQuietly(bufferedSource);
		}

		return null;
//		ByteArrayOutputStream output = null;
//        BufferedInputStream in = null;
//		try {
//			output = new ByteArrayOutputStream();
//			in = new BufferedInputStream(new FileInputStream(file));
//
//			byte[] buff = new byte[1024];
//			int count = 0;
//			while((count=in.read(buff))>0){
//				 output.write(buff,0,count);
//			}
//			byte[] bytes = output.toByteArray();
//			byte keys[] = {(byte) 0xCE, (byte) 0xD3, 'n', 'i', '@', 'Z', 'a', 'w', '^', '2', 't', 'G', 'Q', '6', (byte) 0xA5, (byte) 0xBC};
//			for(int i = 0; i < bytes.length; i++)
//	        {
//	        	byte ccc = bytes[i];
//	        	byte f =  (byte) (ccc ^ keys[i % 16]);
//		        bytes[i] = f;
//	        }
//	        output.close();
//            IOUtils.closeQuietly(output);
//            IOUtils.closeQuietly(in);
//			return new String(bytes,"UTF-8");
//		}catch (IOException e) {
//		}
//        return null;
	}
	/**
	 * 根据文件来初始化
	 *
	 * @param file
	 *            文件
	 */
	private void init(File file) {
    	String lrcstring = decodeLrc(file);
		init(lrcstring);
	}

	/**
	 * 最重要的一个方法，它根据读到的歌词内容 进行初始化，比如把歌词一句一句分开并计算好时间
	 *
	 * @param content
	 *            歌词内容
	 */
	private void init(String content) {
		// 如果歌词的内容为空,则后面就不用执行了
		// 直接显示歌曲名就可以了
		if (content == null || content.trim().equals("")) {
			list.add(new Sentence(songname, Integer.MIN_VALUE,
					Integer.MAX_VALUE));
			return;
		}
		try {
			BufferedReader br = new BufferedReader(new StringReader(content));
			String temp = null;
			while ((temp = br.readLine()) != null) {
				String sentence = temp.trim().replaceAll("<.*,.*,.*>", "");
				parseLine(sentence);
			}
			br.close();
			// 读进来以后就排序了
			Collections.sort(list, new Comparator<Sentence>() {

				public int compare(Sentence o1, Sentence o2) {
					return (int) (o1.getFromTime() - o2.getFromTime());
				}
			});
			// 处理第一句歌词的起始情况,无论怎么样,加上歌名做为第一句歌词,并把它的
			// 结尾为真正第一句歌词的开始
			if (list.size() == 0) {
				list.add(new Sentence(songname, 0,
						Integer.MAX_VALUE));
				return;
			} else {
				Sentence first = list.get(0);
				list.add(
						0,
						new Sentence(songname, 0, first
								.getFromTime()));
			}

			int size = list.size();
			for (int i = 0; i < size; i++) {
				Sentence next = null;
				if (i + 1 < size) {
					next = list.get(i + 1);
				}
				Sentence now = list.get(i);
				if (next != null) {
					now.setToTime(next.getFromTime() - 1);
				}
			}
			// 如果就是没有怎么办,那就只显示一句歌名了
			if (list.size() == 1) {
				list.get(0).setToTime(Integer.MAX_VALUE);
			} else {
				Sentence last = list.get(list.size() - 1);
				last.setToTime(1000);
			}
		} catch (Exception ex) {
			Logger.getLogger(Lyric.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * 分析出整体的偏移量
	 *
	 * @param str
	 *            包含内容的字符串
	 * @return 偏移量，当分析不出来，则返回最大的正数
	 */
	private int parseOffset(String str) {
		String[] ss = str.split("\\:");
		if (ss.length == 2) {
			if (ss[0].equalsIgnoreCase("offset")) {
				int os = Integer.parseInt(ss[1]);
				System.err.println("整体的偏移量：" + os);
				return os;
			} else {
				return Integer.MAX_VALUE;
			}
		} else {
			return Integer.MAX_VALUE;
		}
	}

	/**
	 * 分析这一行的内容，根据这内容 以及标签的数量生成若干个Sentence对象 当此行中的时间标签分布不在一起时，也要能分析出来 所以更改了一些实现
	 * 20080824更新
	 *
	 * @param line
	 *            这一行
	 */
	private void parseLine(String line) {
		if (line.equals("")) {
			return;
		}
		Matcher matcher = pattern.matcher(line);
		List<String> temp = new ArrayList<String>();
		int lastIndex = -1;// 最后一个时间标签的下标
		int lastLength = -1;// 最后一个时间标签的长度
		while (matcher.find()) {
			String s = matcher.group();
			int index = line.indexOf("[" + s + "]");
			if (lastIndex != -1 && index - lastIndex > lastLength + 2) {
				// 如果大于上次的大小，则中间夹了别的内容在里面
				// 这个时候就要分段了
				String content = line.substring(lastIndex + lastLength + 2,
						index);
				for (String str : temp) {
					long t = parseTime(str);
					if (t != -1) {
						System.out.println("content = " + content);
						System.out.println("t = " + t);
						list.add(new Sentence(content, t));
					}
				}
				temp.clear();
			}
			temp.add(s);
			lastIndex = index;
			lastLength = s.length();
		}
		// 如果列表为空，则表示本行没有分析出任何标签
		if (temp.isEmpty()) {
			return;
		}
		try {
			int length = lastLength + 2 + lastIndex;
			String content = line.substring(length > line.length() ? line
					.length() : length);
			// if (Config.getConfig().isCutBlankChars()) {
			// content = content.trim();
			// }
			// 当已经有了偏移量的时候，就不再分析了
			if (content.equals("") && offset == 0) {
				for (String s : temp) {
					int of = parseOffset(s);
					if (of != Integer.MAX_VALUE) {
						offset = of;
						break;// 只分析一次
					}
				}
				return;
			}
			for (String s : temp) {
				long t = parseTime(s);
				if (t != -1) {
					list.add(new Sentence(content, t));
					System.out.println("content = " + content);
					System.out.println("t = " + t);
				}
			}
		} catch (Exception exe) {
		}
	}

	/**
	 * 把如00:00.00这样的字符串转化成 毫秒数的时间，比如 01:10.34就是一分钟加上10秒再加上340毫秒 也就是返回70340毫秒
	 *
	 * @param time
	 *            字符串的时间
	 * @return 此时间表示的毫秒
	 */
	private long parseTime(String time) {
		String[] ss = time.split("\\,|\\.");
		// 如果 是两位以后，就非法了
		if (ss.length < 2) {
			return -1;
		} else if (ss.length == 2) {// 如果正好两位，就算分秒
			try {
				// 先看有没有一个是记录了整体偏移量的
				if (offset == 0 && ss[0].equalsIgnoreCase("offset")) {
					offset = Integer.parseInt(ss[1]);
					System.err.println("整体的偏移量：" + offset);
					return -1;
				}
                return Integer.parseInt(ss[0]);
//				int sec = Integer.parseInt(ss[1]);
//				if (min < 0 || sec < 0 || sec >= 60) {
//					throw new RuntimeException("数字不合法!");
//				}
//				// System.out.println("time" + (min * 60 + sec) * 1000L);
//				return (min * 60 + sec) * 1000L;
			} catch (Exception exe) {
				return -1;
			}
		} else {// 否则也非法
			return -1;
		}
	}

	/**
	 * 得到是否初始化完成了
	 *
	 * @return 是否完成
	 */
	public boolean isInitDone() {
		return initDone;
	}

	/**
	 * 得到当前正在播放的那一句的下标 不可能找不到，因为最开头要加一句 自己的句子 ，所以加了以后就不可能找不到了
	 *
	 * @return 下标
	 */
	int getNowSentenceIndex(long t) {
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).isInTime(t)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 是否能拖动,只有有歌词才可以被拖动,否则没有意义了
	 *
	 * @return 能否拖动
	 */
	public boolean canMove() {
		return list.size() > 1 && enabled;
	}

	/**
	 * 得到当前的时间,一般是由显示面板调用的
	 */
	public long getTime() {
		return tempTime;
	}

}
