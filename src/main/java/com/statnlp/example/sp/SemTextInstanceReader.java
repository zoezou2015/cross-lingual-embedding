/** Statistical Natural Language Processing System
    Copyright (C) 2014-2015  Lu, Wei

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.statnlp.example.sp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.statnlp.commons.types.Sentence;
import com.statnlp.commons.types.WordToken;
import com.statnlp.hybridnetworks.NetworkConfig;

/**
 * @author wei_lu
 *
 */
public class SemTextInstanceReader {

	public static double PRIOR_WEIGHT = 100.0;

	// public static ArrayList<SemanticUnit> _allUnits;

	private static int _maxHeight = -1;

	public static void main(String args[]) throws IOException {

		String lang = "en";

		String filename_inst = "data/geoquery/geoFunql-" + lang + ".corpus";
		String filename_init = "data/geoquery/geoFunql-" + lang + ".init.corpus";

		SemTextDataManager dm = new SemTextDataManager();

		String train_ids = "data/geoquery-2012-08-27/splits/split-880/run-0/fold-0/train-N600";
		ArrayList<SemTextInstance> instances_init = readInit(filename_init, dm);
		ArrayList<SemTextInstance> instances_inst = read(filename_inst, dm, train_ids, true);
		train_ids = "data/geoquery-2012-08-27/splits/split-880/run-0/fold-0/test";
		read(filename_inst, dm, train_ids, true);

		// System.err.println(instances_inst.size()+" instances.");
		// System.err.println(instances_init.size()+" instances.");
		//
		// for(int k = 0; k<instances_inst.size(); k++){
		// SemTextInstance instance = instances_inst.get(k);
		// System.err.println(k+"\t"+instance.getInstanceId());
		// System.err.println(k+"\t"+instance.getWeight());
		// System.err.println(k+"\t"+instance.getInput());
		// System.err.println(k+"\t"+instance.getOutput());
		// }
		//
		// for(int k = 0; k<instances_init.size(); k++){
		// SemTextInstance instance = instances_init.get(k);
		// System.err.println(k+"\t"+instance.getInstanceId());
		// System.err.println(k+"\t"+instance.getWeight());
		// System.err.println(k+"\t"+instance.getInput());
		// System.err.println(k+"\t"+instance.getOutput());
		// }
		//
		// ArrayList<SemanticType> types = dm.getAllTypes();
		// for(SemanticType type : types){
		// System.err.println(type+"\t"+type.getId());
		// }
		//
		// ArrayList<SemanticUnit> units = dm.getAllUnits();
		// for(SemanticUnit unit : units){
		// System.err.println(unit.getMRL()+"\t"+unit.toString()+"\t"+unit.getId());
		// }
		//
		// SemanticForest forest = toForest(dm);
		// System.err.println("Okay forest created..");
		// System.err.println(forest.getAllNodes().size()+" nodes..");
	}

	public static ArrayList<SemTextInstance> readPrior(int startId, String filename, SemTextDataManager dm)
			throws IOException {

		double priorWeight = 0;// 0.01;

		double max_entry = Double.NEGATIVE_INFINITY;
		double min_entry = Double.POSITIVE_INFINITY;

		int id = startId;
		ArrayList<SemTextInstance> instances = new ArrayList<>();

		File f = new File(filename);
		if (!f.exists()) {
			return instances;
		}

		BufferedReader scan = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF8"));
		String line;
		while ((line = scan.readLine()) != null) {
			int bIndex = line.indexOf("[");
			int eIndex = line.indexOf("]");
			String word = line.substring(0, bIndex).trim();
			WordToken wordToken = new WordToken(word);
			String[] vecStr = line.substring(bIndex + 1, eIndex).split("\\s");
			double[] vecs = new double[vecStr.length];
			for (int k = 0; k < vecs.length; k++) {
				vecs[k] = Double.parseDouble(vecStr[k]);
				if (min_entry > vecs[k]) {
					min_entry = vecs[k];
				}
				if (max_entry < vecs[k]) {
					max_entry = vecs[k];
				}
				// vecs[k] = priorWeight * Math.exp(vecs[k]);
				vecs[k] = priorWeight * vecs[k];
				SemTextPriorInstance inst = new SemTextPriorInstance(id++, vecs[k], k, wordToken);
				instances.add(inst);
				// System.err.println(vecs[k]);
			}
		}
		scan.close();
		// System.err.println(instances.size()+" instances.");
		// System.err.println("max_entry="+max_entry);
		// System.err.println("min_entry="+min_entry);
		// System.exit(1);
		return instances;

	}

	public static ArrayList<SemTextInstance> read(String filename, SemTextDataManager dm, String ids_filename,
			boolean isTrain) throws IOException {
		// Scanner scan = new Scanner(new File(ids_filename));
		BufferedReader scan = new BufferedReader(new InputStreamReader(new FileInputStream(ids_filename), "UTF8"));
		ArrayList<Integer> ids = new ArrayList<>();
		String line;
		while ((line = scan.readLine()) != null) {
			int id = Integer.parseInt(line);
			ids.add(id);
		}
		// while(scan.hasNextLine()){
		// String line = scan.nextLine();
		// int id = Integer.parseInt(line);
		// ids.add(id);
		// }
		scan.close();

		ArrayList<SemTextInstance> instances = new ArrayList<>();

		scan = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF8"));
		// scan = new Scanner(new File(filename));
		while ((line = scan.readLine()) != null) {

			if (line.startsWith("id:")) {
				int index = line.indexOf(":");
				int id = Integer.parseInt(line.substring(index + 1).trim());
				if (!ids.contains(id)) {
					continue;
				}
				String[] words = scan.readLine().substring(3).trim().split("\\s");
				WordToken[] wTokens = new WordToken[words.length];
				for (int k = 0; k < words.length; k++) {
					wTokens[k] = new WordToken(words[k]);
				}
				Sentence sent = new Sentence(wTokens);
				String mrl = scan.readLine().substring(4).trim();
				scan.readLine();
				ArrayList<String> prods_form = new ArrayList<>();
				while ((line = scan.readLine()).startsWith("*"))
					prods_form.add(line);
				SemanticForest tree = toTree(id, prods_form, dm);
				SemTextInstance inst = new SemTextInstance(id >= 0 ? id + 1 : id, id >= 0 ? 1.0 : 100.0, sent, tree,
						mrl);
				instances.add(inst);

				int height = tree.getHeight();
				if (_maxHeight < height) {
					_maxHeight = height;
				}

				if (id >= 0) {
					String sentence = sent.toString();
					ArrayList<SemanticForestNode> nodes = tree.getAllNodes();
					for (int k = 0; k < nodes.size(); k++) {
						if (nodes.get(k).isRoot())
							continue;
						SemanticUnit unit = nodes.get(k).getUnit();
						if (unit.isContextIndependent()) {
							boolean found_unit_phrase = false;
							ArrayList<String> phrases = dm.getPriorUnitToPhrases(unit);
							ArrayList<String> tmp = new ArrayList<>();
							for (String s : sentence.split(" ")) {
								tmp.add(s);
							}

							for (String phrase : phrases) {
								if (phrase.split(" ").length > 1 && sentence.indexOf(phrase) != -1
										|| tmp.contains(phrase)) {
									found_unit_phrase = true;
								}
							}
							if (!found_unit_phrase) {
								System.err.println("did not find the phrase in instance " + id);
								System.err.println("sentence: " + sentence + ", unit: " + unit);
								// System.exit(1);
							} else {
								// System.err.println(sentence);
								// System.err.println(phrases.toString());
							}
						}
					}
				}

			}
		}
		// while(scan.hasNextLine()){
		// String line = scan.nextLine();
		//
		// if(line.startsWith("id:")){
		// int index = line.indexOf(":");
		// int id = Integer.parseInt(line.substring(index+1).trim());
		// if(!ids.contains(id)){
		// continue;
		// }
		// String[] words = scan.nextLine().substring(3).trim().split("\\s");
		// WordToken[] wTokens = new WordToken[words.length];
		// for(int k = 0; k<words.length; k++){
		// wTokens[k] = new WordToken(words[k]);
		// }
		// Sentence sent = new Sentence(wTokens);
		// String mrl = scan.nextLine().substring(4).trim();
		// scan.nextLine();
		// ArrayList<String> prods_form = new ArrayList<String>();
		// while((line=scan.nextLine()).startsWith("*"))
		// prods_form.add(line);
		// SemanticForest tree = toTree(id, prods_form, dm);
		// SemTextInstance inst = new SemTextInstance(id>=0? id+1 : id, id>=0? 1.0 :
		// 100.0, sent, tree, mrl);
		// instances.add(inst);
		//
		// int height = tree.getHeight();
		// if(_maxHeight < height){
		// _maxHeight = height;
		// }
		// }
		// }
		scan.close();

		if (isTrain) {
			dm.fixSemanticUnits();
		}

		System.err.println("maxHeight=\t" + _maxHeight);

		return instances;
	}

	public static ArrayList<SemTextInstance> readInit(String filename, SemTextDataManager dm)
			throws NumberFormatException, IOException {

		ArrayList<SemTextInstance> instances = new ArrayList<>();

		// Scanner scan = new Scanner(new File(filename));
		BufferedReader scan = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF8"));
		String line;
		while ((line = scan.readLine()) != null) {

			// System.err.println(line);

			if (line.startsWith("id:")) {
				int index = line.indexOf(":");
				int id = Integer.parseInt(line.substring(index + 1).trim());
				// System.err.println("OK"+id+"["+line+"]");
				String[] words = scan.readLine().substring(3).trim().split("\\s");
				WordToken[] wTokens = new WordToken[words.length];
				for (int k = 0; k < words.length; k++) {
					wTokens[k] = new WordToken(words[k]);
				}
				Sentence sent = new Sentence(wTokens);
				String mrl = scan.readLine().substring(4).trim();
				scan.readLine();
				ArrayList<String> prods_form = new ArrayList<>();
				while ((line = scan.readLine()).startsWith("*"))
					prods_form.add(line);
				SemanticForest tree = toTree(id, prods_form, dm);
				SemTextInstance inst = new SemTextInstance(id >= 0 ? id + 1 : id, id >= 0 ? 1.0 : PRIOR_WEIGHT, sent,
						tree, mrl);
				if (id < 0) {
					String phrase = sent.toString();
					SemanticUnit unit = tree.getRoot().getChildren()[0][0].getUnit();
					dm.addPriorUnitToPhrases(unit, phrase);
				}
				instances.add(inst);
			}
		}

		// while(scan.hasNextLine()){
		// String line = scan.nextLine();
		//
		// if(line.startsWith("id:")){
		// int index = line.indexOf(":");
		// int id = Integer.parseInt(line.substring(index+1).trim());
		// String[] words = scan.nextLine().substring(3).trim().split("\\s");
		// WordToken[] wTokens = new WordToken[words.length];
		// for(int k = 0; k<words.length; k++){
		// wTokens[k] = new WordToken(words[k]);
		// }
		// Sentence sent = new Sentence(wTokens);
		// String mrl = scan.nextLine().substring(4).trim();
		// scan.nextLine();
		// ArrayList<String> prods_form = new ArrayList<String>();
		// while((line=scan.nextLine()).startsWith("*"))
		// prods_form.add(line);
		// SemanticForest tree = toTree(id, prods_form, dm);
		// SemTextInstance inst = new SemTextInstance(id>=0? id+1 : id, id>=0? 1.0 :
		// PRIOR_WEIGHT, sent, tree, mrl);
		// instances.add(inst);
		// }
		// }
		scan.close();

		return instances;
	}

	// create the global forest.
	// bottom-up approach.
	public static SemanticForest toForest(SemTextDataManager dm) {

		boolean checkValidPair = true;
		// boolean checkValidPair = false;

		ArrayList<SemanticUnit> units = dm.getAllUnits();

		ArrayList<SemanticForestNode> nodes_at_prev_depth = new ArrayList<>();

		for (int dIndex = 1; dIndex < NetworkConfig._SEMANTIC_FOREST_MAX_DEPTH; dIndex++) {

			ArrayList<SemanticForestNode> nodes_at_curr_depth = new ArrayList<>();

			for (int wIndex = 0; wIndex < units.size(); wIndex++) {
				SemanticUnit unit = units.get(wIndex);
				SemanticForestNode node = new SemanticForestNode(unit, dIndex);

				if (node.arity() == 0) {
					// always adds it since it does not require children.
					nodes_at_curr_depth.add(node);
				} else if (node.arity() == 1) {
					ArrayList<SemanticForestNode> node_children_0 = new ArrayList<>();

					for (int k = 0; k < nodes_at_prev_depth.size(); k++) {
						SemanticForestNode node_child = nodes_at_prev_depth.get(k);
						if (checkValidPair) {
							if (dm.isValidUnitPair(node.getUnit(), node_child.getUnit(), 0))
								node_children_0.add(node_child);
						} else {
							if (node_child.getUnit().getLHS().equals(node.getUnit().getRHS()[0]))
								node_children_0.add(node_child);
						}
					}

					if (node_children_0.size() == 0) {
						// ignore since the children is empty..
					} else {
						SemanticForestNode[] children0 = new SemanticForestNode[node_children_0.size()];
						for (int k = 0; k < children0.length; k++) {
							children0[k] = node_children_0.get(k);
						}
						node.setChildren(0, children0);
						nodes_at_curr_depth.add(node);
					}

				} else if (node.arity() == 2) {
					ArrayList<SemanticForestNode> node_children_0 = new ArrayList<>();
					ArrayList<SemanticForestNode> node_children_1 = new ArrayList<>();
					for (int k = 0; k < nodes_at_prev_depth.size(); k++) {
						SemanticForestNode node_child = nodes_at_prev_depth.get(k);
						if (checkValidPair) {
							if (dm.isValidUnitPair(node.getUnit(), node_child.getUnit(), 0))
								node_children_0.add(node_child);
						} else {
							if (node_child.getUnit().getLHS().equals(node.getUnit().getRHS()[0]))
								node_children_0.add(node_child);
						}

						if (checkValidPair) {
							if (dm.isValidUnitPair(node.getUnit(), node_child.getUnit(), 1))
								node_children_1.add(node_child);
						} else {
							if (node_child.getUnit().getLHS().equals(node.getUnit().getRHS()[1]))
								node_children_1.add(node_child);
						}
					}

					if (node_children_0.size() == 0 || node_children_1.size() == 0) {
						// ignore since some children can not be found..
					} else {
						SemanticForestNode[] children0 = new SemanticForestNode[node_children_0.size()];
						for (int k = 0; k < children0.length; k++) {
							children0[k] = node_children_0.get(k);
						}
						node.setChildren(0, children0);

						SemanticForestNode[] children1 = new SemanticForestNode[node_children_1.size()];
						for (int k = 0; k < children1.length; k++) {
							children1[k] = node_children_1.get(k);
						}
						node.setChildren(1, children1);

						nodes_at_curr_depth.add(node);
					}

				} else {
					throw new RuntimeException("The arity is " + node.arity());
				}
			}

			nodes_at_prev_depth = nodes_at_curr_depth;
		}

		// System.err.println(nodes_at_prev_depth.size());

		SemanticForestNode root = SemanticForestNode.createRootNode(NetworkConfig._SEMANTIC_FOREST_MAX_DEPTH);

		ArrayList<SemanticUnit> rootUnits = dm.getRootUnits();
		ArrayList<SemanticForestNode> rootNodes = new ArrayList<>();
		for (SemanticForestNode node : nodes_at_prev_depth) {
			SemanticUnit unit = node.getUnit();
			if (rootUnits.contains(unit)) {
				rootNodes.add(node);
			}
		}
		SemanticForestNode[] roots = new SemanticForestNode[rootNodes.size()];
		for (int k = 0; k < roots.length; k++)
			roots[k] = rootNodes.get(k);
		root.setChildren(0, roots);

		SemanticForest forest = new SemanticForest(root);

		return forest;
	}

	public static SemanticForest toTree(int id, ArrayList<String> prods_form, SemTextDataManager dm) {

		SemanticForestNode[] nodes = new SemanticForestNode[prods_form.size()];
		toSemanticNode(prods_form, 0, nodes, dm, 1);

		for (int k = nodes.length - 1; k >= 0; k--) {
			nodes[k].setId(nodes.length - 1 - k);
		}

		// add the root unit.
		if (id >= 0) {
			dm.recordRootUnit(nodes[0].getUnit());
		} else {
			nodes[0].getUnit().setContextIndependent();
		}

		SemanticForestNode root = SemanticForestNode.createRootNode(NetworkConfig._SEMANTIC_FOREST_MAX_DEPTH);
		root.setChildren(0, new SemanticForestNode[] { nodes[0] });

		// //if it's the prior instance, then set it as context independent.
		// if(id<0){
		// nodes[0].getUnit().setContextIndependent();
		// }

		if (id >= 0) {
			addValidUnitPairs(root, dm);
		}

		SemanticForest tree = new SemanticForest(root);

		return tree;

	}

	private static void addValidUnitPairs(SemanticForestNode parent, SemTextDataManager dm) {
		SemanticForestNode[][] children = parent.getChildren();
		for (int k = 0; k < children.length; k++) {
			SemanticForestNode child = children[k][0];
			dm.addValidUnitPair(parent.getUnit(), child.getUnit(), k);
			addValidUnitPairs(child, dm);
		}
	}

	private static void toSemanticNode(ArrayList<String> prods_form, int pos, SemanticForestNode[] nodes,
			SemTextDataManager dm, int depth) {

		int maxDepth = NetworkConfig._SEMANTIC_FOREST_MAX_DEPTH;

		String prod_form = prods_form.get(pos);
		SemanticUnit unit = toSemanticUnit(prod_form, dm);
		if (maxDepth - depth <= 0)
			throw new RuntimeException("The depth is " + depth + "!");

		SemanticForestNode node = new SemanticForestNode(unit, maxDepth - depth);
		nodes[pos] = node;

		if (node.arity() == 0) {
		} else if (node.arity() == 1) {
			toSemanticNode(prods_form, pos + 1, nodes, dm, depth + 1);
			node.setChildren(0, new SemanticForestNode[] { nodes[pos + 1] });
		} else if (node.arity() == 2) {
			toSemanticNode(prods_form, pos + 1, nodes, dm, depth + 1);
			int num_children = nodes[pos + 1].countAllChildren() + 1;
			toSemanticNode(prods_form, pos + num_children, nodes, dm, depth + 1);
			node.setChildren(0, new SemanticForestNode[] { nodes[pos + 1] });
			node.setChildren(1, new SemanticForestNode[] { nodes[pos + num_children] });
		}

	}

	// *n:Query -> ({ answer ( *n:State ) })
	private static SemanticUnit toSemanticUnit(String form, SemTextDataManager dm) {

		String mrl = form;
		int index;

		index = form.indexOf("({");
		String rhs_string = form.substring(index + 2).trim();
		index = rhs_string.lastIndexOf("})");
		rhs_string = rhs_string.substring(0, index).trim();
		// System.err.println(rhs_string);
		String[] rhs_tokens = rhs_string.split("\\s");
		// System.exit(1);

		index = form.indexOf("->");
		String lhs = form.substring(0, index).trim();
		form = form.substring(index + 2).trim();
		form = form.substring(2, form.length() - 2);
		index = form.lastIndexOf("(");
		String name[];
		if (index == -1) {
			name = new String[] { form };
		} else {
			name = form.substring(0, index).trim().split("\\(");
		}
		String[] tokens = form.substring(index + 1).trim().split("\\s");
		ArrayList<String> tokens_s = new ArrayList<>();
		for (String token : tokens) {
			if (token.startsWith("*")) {
				tokens_s.add(token);
			}
		}
		String[] rhs = new String[tokens_s.size()];
		for (int k = 0; k < rhs.length; k++) {
			rhs[k] = tokens_s.get(k);
		}
		SemanticUnit unit = dm.toSemanticUnit(lhs, name, rhs, mrl, rhs_tokens);
		// rhs: debug
		// if (name.length > 1 || rhs.length > 1) {
		// System.out.println("mrl: "+mrl);
		// System.out.println("lhs: "+lhs);
		// System.out.println("name: "+Arrays.toString(name));
		// System.out.println("rhs: "+Arrays.toString(rhs));
		// System.out.println("rhs_tokens: "+Arrays.toString(rhs_tokens));
		// }
		return unit;

	}

}
