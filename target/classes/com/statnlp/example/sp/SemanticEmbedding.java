package com.statnlp.example.sp;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;

import com.statnlp.commons.types.Sentence;

public class SemanticEmbedding {

	public static void main(String args[]) throws IOException {
		String writeFilePath = "data/semantic_output_noinit_20.txt";

		BufferedWriter bw = new BufferedWriter(new FileWriter(writeFilePath));
		SemanticEmbedding.LANGUAGE = new String[] { "de", "el", "en", "zh", "id", "sv", "fa" };
		SemanticEmbedding se = new SemanticEmbedding(20);

		se.loadInstances();
		HashMap<String, Integer> semantic2id = se.getSemantic2IdMap();
		int count = 0;
		for (String output : semantic2id.keySet()) {
			if (output.contains("'"))
				continue;
			// int id = semantic2id.get(output);
			// String output_line = id + "\n";
			// System.out.println(output_line);
			// bw.write(output_line);

			double[] vector = se.getEmbeddingVector(output);
			if (vector != null) {
				StringBuilder sb = new StringBuilder();
				output = output.replace("*n:", "");
				output = output.replace(" -> ({", " :");
				output = output.replace(" })", "");
				sb.append(output + "\t");
				for (int i = 0; i < vector.length; i++) {
					sb.append(vector[i]);
					sb.append("\t");
				}
				sb.append("\n");
				String line = sb.toString();
				bw.write(line);
				count++;
				// if (count > 15)
				// break;
			} else {
				continue;
			}

		}
		bw.close();
		// for (String output : semantic2id.keySet()) {
		// double[] vector = se.getMatrix(output);
		// double[] truncatedVector = se.getEmbeddingVector(output);
		// // System.out.println("Semantic: " + output);
		// // System.out.println("Occurance Matrix: " + Arrays.toString(vector));
		// // System.out.println("Truncated Matrix: " +
		// Arrays.toString(truncatedVector));
		// // if (output.contains("'"))
		// // continue;
		// // int id = semantic2id.get(output);
		// // String output_line = id + "\n";
		// // System.out.println(output_line);
		// // bw.write(output_line);
		// bw.write("Semantic: " + output + "\n");
		//
		// if (vector != null) {
		// StringBuilder sb = new StringBuilder();
		// sb.append("Occurance Matrix: " + "\t");
		// for (int i = 0; i < vector.length; i++) {
		// sb.append(vector[i]);
		// sb.append("\t");
		// }
		// sb.append("\n");
		// String line = sb.toString();
		// bw.write(line);
		// // if (count > 15)
		// // break;
		// }
		// if (truncatedVector != null) {
		// StringBuilder sb = new StringBuilder();
		// sb.append("Truncated Matrix: " + "\t");
		// for (int i = 0; i < truncatedVector.length; i++) {
		// sb.append(truncatedVector[i]);
		// sb.append("\t");
		// }
		// sb.append("\n");
		// String line = sb.toString();
		// bw.write(line);
		// // if (count > 15)
		// // break;
		// }
		// }
		// bw.close();
	}

	public static String[] LANGUAGE = { "en" };
	public static String TRAIN_IDS = "data/geoquery-2012-08-27/splits/split-880/run-0/fold-0/train-N600";
	/**
	 * Map from feature semantic to [a map from word to counts for the semantic-word
	 * pair]
	 */
	// protected HashMap<String, HashMap<String, Integer>> _semantic2wrod2count
	// = new HashMap<String, HashMap<String, Integer>>();
	protected HashMap<Integer, HashMap<Integer, Integer>> _semanticIdx2wordIdx2Idx = new HashMap<>();

	/**
	 * Map from feature type to [a map from output to [a map from input to feature
	 * ID]]
	 */
	protected HashMap<String, HashMap<String, HashMap<String, Integer>>> _featureIntMap;
	protected double[][] _emissionFeatureArray;
	protected RealMatrix _emissionFeatureMatrix;
	private double[][] _M;
	private double[][] _truncatedM;
	private int _shapeSize;
	protected HashMap<String, Integer> _word2id;
	protected HashMap<String, Integer> _semantic2id;
	protected int _outputSize;
	protected int _inputSize;

	public SemanticEmbedding(int shapeSize) {
		this._shapeSize = shapeSize;
	}

	public int getWordID(String word) {
		if (this._word2id.containsKey(word))
			return this._word2id.get(word);
		else
			return -1;
	}

	public int getSemanticID(String semantic) {
		if (this._semantic2id.containsKey(semantic))
			return this._semantic2id.get(semantic);
		else
			return -1;
	}

	public HashMap<String, Integer> getSemantic2IdMap() {
		return this._semantic2id;
	}

	// e.g. loadInstances(new String[] {"de","el","th"},...)
	public void loadInstances() {
		ArrayList<SemTextInstance> all_insts = new ArrayList<>();
		String[] langs = this.LANGUAGE;
		for (String lang : langs) {

			String inst_filename = "data/geoquery/geoFunql-" + lang + ".corpus";

			SemTextDataManager dm = new SemTextDataManager();
			ArrayList<SemTextInstance> insts_train = null;
			try {
				insts_train = SemTextInstanceReader.read(inst_filename, dm, this.TRAIN_IDS, true);
				all_insts.addAll(insts_train);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// first pass update vocab statistics
		HashMap<String, Integer> semantic2id = null;
		HashMap<String, Integer> word2id = null;
		if (this._semantic2id == null) {
			semantic2id = new HashMap<>();
		} else {
			semantic2id = this._semantic2id;
		}
		if (this._word2id == null) {
			word2id = new HashMap<>();
		} else {
			word2id = this._word2id;
		}

		for (int i = 0; i < all_insts.size(); i++) {
			SemTextInstance inst = all_insts.get(i);
			int langId = i / (all_insts.size() / langs.length);
			String lang = langs[langId];
			Sentence input = inst.getInput();
			SemanticForest output = inst.getOutput();
			for (int node_i = 0; node_i < output.getAllNodes().size(); node_i++) {
				SemanticForestNode node = output.getAllNodes().get(node_i);
				if (node.isRoot()) {
					continue;
				}
				if (node != null) {
					if (node.getUnitToString() != null) {
						// System.out.println(node.getNameOnly());
						String y = node.getUnitToString();
						if (!semantic2id.containsKey(y)) {
							semantic2id.put(y, semantic2id.size());
						}
					}
				}
			}
			for (int j = 0; j < input.length(); j++) {
				String x = lang + "_" + input.get(j).getName();
				// System.out.println(x);
				if (!word2id.containsKey(x)) {
					word2id.put(x, word2id.size());
				}
			}
		}

		// for (String y : semantic2id.keySet()) {
		// System.out.println("y: " + y + " id: " + semantic2id.get(y));
		// }
		// for (String y : word2id.keySet()) {
		// System.out.println("y: " + y + " id: " + word2id.get(y));
		// }
		// System.out.println("all_insts.size() "+ all_insts.size());
		// System.out.println("row and column: "+ semantic2id.size()+" "+
		// word2id.size());

		this._semantic2id = semantic2id;
		this._word2id = word2id;
		// second pass for counting
		this._M = new double[this._semantic2id.size()][this._word2id.size()];
		for (int i = 0; i < all_insts.size(); i++) {
			SemTextInstance inst = all_insts.get(i);
			int langId = i / (all_insts.size() / langs.length);
			String lang = langs[langId];
			Sentence input = inst.getInput();
			SemanticForest output = inst.getOutput();
			for (int node_i = 0; node_i < output.getAllNodes().size(); node_i++) {
				SemanticForestNode node = output.getAllNodes().get(node_i);
				if (node.isRoot()) {
					continue;
				}
				if (node != null) {
					if (node.getUnitToString() != null) {
						String y = node.getUnitToString();
						int yIdx = this._semantic2id.get(y);
						for (int j = 0; j < input.length(); j++) {
							String x = lang + "_" + input.get(j).getName();
							int xIdx = this._word2id.get(x);
							this._M[yIdx][xIdx]++;
						}
					}
				}
			}
		}
		this._truncatedM = getTruncatedSVD(this._M, this._shapeSize);
	}

	public double getEmbeddingValueForDim(String output, int dim) {
		// System.out.println("===inside getEmbeddingValueForDim ======");

		int outputID = getSemanticID(output);
		// System.out.println("output id: " + outputID + " " + output);
		if (outputID >= 0) {
			// System.out.println("output : " +
			// this._truncatedM[outputID][dim]);
			return this._truncatedM[outputID][dim];
		} else {
			return 0.0;
		}
	}

	public double[] getMatrix(String output) {
		// System.out.println("===inside getEmbeddingValueForDim ======");

		int outputID = getSemanticID(output);
		// System.out.println("output id: " + outputID + " " + output);
		if (outputID >= 0) {
			// System.out.println("output : " +
			// this._truncatedM[outputID][dim]);
			return this._M[outputID];
		} else {
			return null;
		}
	}

	public double[] getEmbeddingVector(String output) {
		int outputID = getSemanticID(output);
		// System.out.println("output id: " + outputID + " " + output);
		if (outputID >= 0) {
			// System.out.println("output : " +
			// this._truncatedM[outputID][dim]);
			return this._truncatedM[outputID];
		} else {
			return null;
		}
	}

	public static double[][] getTruncatedSVD(double[][] matrix, int k) {
		// System.out.println("===inside getTruncatedSVD ======");

		RealMatrix m = new Array2DRowRealMatrix(matrix);
		if (k > m.getColumnDimension() || k > m.getRowDimension()) {
			k = Math.min(m.getColumnDimension(), m.getRowDimension());
		}
		SingularValueDecomposition svd = new SingularValueDecomposition(m);

		double[][] truncatedU = new double[svd.getU().getRowDimension()][k];
		svd.getU().copySubMatrix(0, truncatedU.length - 1, 0, k - 1, truncatedU);

		double[][] truncatedS = new double[k][k];
		svd.getS().copySubMatrix(0, k - 1, 0, k - 1, truncatedS);

		double[][] truncatedVT = new double[k][svd.getVT().getColumnDimension()];
		svd.getVT().copySubMatrix(0, k - 1, 0, truncatedVT[0].length - 1, truncatedVT);

		// RealMatrix approximatedSvdMatrix = (new Array2DRowRealMatrix(truncatedU))
		// .multiply(new Array2DRowRealMatrix(truncatedS)).multiply(new
		// Array2DRowRealMatrix(truncatedVT));
		RealMatrix approximatedSvdMatrix = (new Array2DRowRealMatrix(truncatedU))
				.multiply(new Array2DRowRealMatrix(truncatedS));
		return approximatedSvdMatrix.getData();
	}

	// unused
	public void constructSemantic2Word2CountMap() {
		System.out.println("===inside constructSemantic2Word2CountMap ======");

		HashMap<String, Integer> word2id = null;
		HashMap<String, Integer> semantic2id = null;
		if (this._word2id != null) {
			word2id = this._word2id;
		} else {
			word2id = new HashMap<>();
		}
		if (this._semantic2id != null) {
			semantic2id = this._semantic2id;
		} else {
			semantic2id = new HashMap<>();
		}
		Iterator<String> types = this._featureIntMap.keySet().iterator();
		System.out.println(this._featureIntMap.size());
		while (types.hasNext()) {
			String type = types.next();
			if (type == "emission") {
				HashMap<String, HashMap<String, Integer>> output2input = this._featureIntMap.get(type);
				Iterator<String> outputs = output2input.keySet().iterator();
				while (outputs.hasNext()) {
					String output = outputs.next();
					if (this._semantic2id == null && !semantic2id.containsKey(output)) {
						semantic2id.put(output, semantic2id.size());
					}
					int semanticID = semantic2id.get(output);

					if (!this._semanticIdx2wordIdx2Idx.containsKey(semanticID)) {
						this._semanticIdx2wordIdx2Idx.put(semanticID, new HashMap<Integer, Integer>());
					}
					// ArrayList<String> phrases =
					// this._dm.getPriorUnit2StringToPhrases(output);
					/* include initial instances by commenting if-clause */
					// if(phrases == null){
					HashMap<String, Integer> input2id = output2input.get(output);
					Iterator<String> inputs = input2id.keySet().iterator();
					while (inputs.hasNext()) {
						String input = inputs.next();
						// don't update word2id if this is not first time
						if (this._word2id == null && !word2id.containsKey(input)) {
							word2id.put(input, word2id.size());
						}
						int wordID = word2id.get(input);
						if (!this._semanticIdx2wordIdx2Idx.get(semanticID).containsKey(wordID)) {
							this._semanticIdx2wordIdx2Idx.get(semanticID).put(wordID, 1);
						} else {
							this._semanticIdx2wordIdx2Idx.get(semanticID).put(wordID,
									this._semanticIdx2wordIdx2Idx.get(semanticID).get(wordID) + 1);
						}
					}
					// }
				}
			}
		}
		this._outputSize = semantic2id.size();
		this._inputSize = word2id.size();
		this._word2id = word2id;
		this._semantic2id = semantic2id;
	}

	// unused
	public void map2Matrix() {
		System.out.println("===inside map2Matrix ======");
		this._emissionFeatureArray = new double[this._outputSize][this._inputSize];
		for (int row = 0; row < this._outputSize; row++) {
			for (int col = 0; col < this._inputSize; col++) {
				int idx = 0;
				if (this._semanticIdx2wordIdx2Idx.containsKey(row)) {
					if (this._semanticIdx2wordIdx2Idx.get(row).containsKey(col))
						idx = this._semanticIdx2wordIdx2Idx.get(row).get(col);
				}
				this._emissionFeatureArray[row][col] = idx;
			}
		}

		RealMatrix n = new Array2DRowRealMatrix(this._emissionFeatureArray);
		this._emissionFeatureMatrix = n;
	}
}
