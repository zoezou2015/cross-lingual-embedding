package com.statnlp.example.sp.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class SplitCVFolds {
	public static void main(String args[]) throws IOException {
		for (int i = 0; i < 3; i++) {
			ArrayList<String> stringList = new ArrayList<>();
			ArrayList<String> test_stringList = new ArrayList<>();

			String train_file = "data/3-fold/fold-" + i + "/train.txt";
			String test_file = "data/3-fold/fold-" + i + "/test.txt";
			for (int j = 0; j < 3; j++) {
				if (j != i) {
					String read_test_file = "data/3-fold/fold" + j;
					String line;
					BufferedReader test_scan = new BufferedReader(
							new InputStreamReader(new FileInputStream(read_test_file)));
					while ((line = test_scan.readLine()) != null) {
						line = line.trim();
						stringList.add(line);
					}
					test_scan.close();
				} else {
					String read_test_file = "data/3-fold/fold" + j;
					String line;
					BufferedReader test_scan = new BufferedReader(
							new InputStreamReader(new FileInputStream(read_test_file)));
					while ((line = test_scan.readLine()) != null) {
						line = line.trim();
						test_stringList.add(line);
					}
					test_scan.close();
				}
			}

			BufferedWriter train_writer = new BufferedWriter(new FileWriter(train_file));
			for (String id : stringList) {
				train_writer.write(id + "\n");
			}
			System.err.println(i + " " + stringList.size());
			train_writer.close();

			BufferedWriter test_writer = new BufferedWriter(new FileWriter(test_file));
			for (String id : test_stringList) {
				test_writer.write(id + "\n");
			}
			System.err.println(i + " " + test_stringList.size());
			test_writer.close();

		}
	}
}
