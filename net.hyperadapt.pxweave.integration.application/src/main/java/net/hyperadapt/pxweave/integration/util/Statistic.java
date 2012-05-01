package net.hyperadapt.pxweave.integration.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.hyperadapt.pxweave.integration.generic.PreProcessingFilter;

/**
 * With the help of this class, its possible to capture the adaptation times
 * regarding to the selenium test (penetration test). Its only a test class,
 * which must be adjusted before using it.
 * 
 * @author Martin Lehmann
 * 
 */
public class Statistic {

	private final static int DEFAULT_SIZE = 11;
	protected static Map<String, List<Long>> statisticMapPre = new HashMap<String, List<Long>>();
	protected static Map<String, List<Long>> statisticMapPost = new HashMap<String, List<Long>>();
	protected static Map<String, List<Long>> statisticMapFull = new HashMap<String, List<Long>>();

	public static boolean readyForWriting = false;

	public static void countPre(String session, Long time) {
		List<Long> sessionList = statisticMapPre.get(session);
		if (sessionList == null) {
			sessionList = new ArrayList<Long>();
		}
		sessionList.add(time);
		statisticMapPre.put(session, sessionList);

		if (sessionList.size() >= DEFAULT_SIZE) {
			readyForWriting = true;
		}
	}

	public static void countPost(String session, Long time) {
		List<Long> sessionList = statisticMapPost.get(session);
		if (sessionList == null) {
			sessionList = new ArrayList<Long>();
		}
		sessionList.add(time);
		statisticMapPost.put(session, sessionList);
	}

	public static void countFull(String begin, String session, Long time) {
		List<Long> sessionList = statisticMapFull.get(begin + session);
		if (sessionList == null) {
			sessionList = new ArrayList<Long>();
		}
		sessionList.add(time);
		statisticMapFull.put(begin + session, sessionList);
	}

	public static synchronized void writeStatistic() {
		try {
			String fileName = "C:/Users/media/Desktop/statisticPre.txt";
			Integer countName = 0;
			while ((new File(fileName)).exists()) {
				fileName = "C:/Users/media/Desktop/statisticPre"
						+ countName.toString() + ".txt";
				countName++;
			}
			FileWriter fstream = new FileWriter(fileName);
			if (PreProcessingFilter.readyForWriting) {
				BufferedWriter out = new BufferedWriter(fstream);

				String currentSession = "";

				out.write("\nPreProcessingFilter: \n");
				for (String entry : PreProcessingFilter.statisticMapPre
						.keySet()) {
					if (!currentSession.equals(entry)) {
						currentSession = entry;
						out.write("\n\n" + currentSession + ":\n");
					}
					List<Long> times = PreProcessingFilter.statisticMapPre
							.get(currentSession);
					Long average = 0L;
					boolean first = true;
					for (Long time : times) {
						if (!first) {
							average += time;
							out.write(time + " ");
						}
						first = false;
					}
					if (average > 0) {
						out.write("\nDurchschnitt: " + average
								/ (times.size() - 1));
					}
				}

				currentSession = "";
				out.write("\n\nPostProcessingFilter: \n");
				for (String entry : PreProcessingFilter.statisticMapPost
						.keySet()) {
					if (!currentSession.equals(entry)) {
						currentSession = entry;
						out.write("\n\n" + currentSession + ":\n");
					}
					List<Long> times = PreProcessingFilter.statisticMapPost
							.get(currentSession);
					int count = 0;
					Long average = 0L;
					boolean first = true;
					for (Long time : times) {
						if (!first && time > 100) {
							average += time;
							out.write(time + " ");
							count++;
						}
						first = false;
					}
					out.write("\nDurchschnitt: " + average / count);
				}

				currentSession = "";
				out.write("\n\nPre- und PostProcessingFilter: \n");
				Map<String, List<List<Long>>> fullMap = new HashMap<String, List<List<Long>>>();

				for (String entry : PreProcessingFilter.statisticMapFull
						.keySet()) {

					if (!currentSession.equals(entry)) {
						currentSession = entry.replace("Start:", "")
								.replace("End:", "").trim();
					}
					int countEnd = 0;
					boolean isEnd = false;
					if (entry.startsWith("End")) {
						isEnd = true;
					}

					List<Long> startList = new ArrayList<Long>();
					List<Long> endList = new ArrayList<Long>();
					List<Long> times = PreProcessingFilter.statisticMapFull
							.get(entry);
					boolean first = true;
					for (Long time : times) {
						if (!first) {
							if (!isEnd) {
								startList.add(time);
							} else if (countEnd % 2 == 1) {
								endList.add(time);
							}
							countEnd++;
						}
						first = false;
					}

					List<List<Long>> sessionList = fullMap.get(currentSession);
					if (sessionList == null) {
						sessionList = new ArrayList<List<Long>>();
					}
					if (!isEnd) {
						if (sessionList.isEmpty()) {
							sessionList.add(startList);
						} else {
							sessionList.add(0, startList);
						}
					} else {
						sessionList.add(endList);
					}
					fullMap.put(currentSession, sessionList);
				}

				for (String session : fullMap.keySet()) {
					out.write("\n\n" + session + ":\n");

					Long average = 0L;
					int count = fullMap.get(session).get(0).size();
					for (int i = 0; i < count; i++) {
						Long start = fullMap.get(session).get(1).get(i);
						Long end = fullMap.get(session).get(0).get(i);
						Long erg = end - start;
						average += erg;
						out.write(end + " - " + start + " = " + erg + ":\n");
					}
					out.write("\nDurchschnitt: " + average / count);
				}

				out.close();

				// PreProcessingFilter.statisticMapPre.clear();
				// PreProcessingFilter.statisticMapPost.clear();
				// PreProcessingFilter.statisticMapFull.clear();
				// PreProcessingFilter.readyForWriting = false;
			}
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
	}

}
