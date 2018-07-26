# The StatNLP Semantic Parser Version 0.3-n

Code for the ACL-18 paper: Learning Cross-lingual Distributed Logical Representations for Semantic Parsing. This paper learns the distributed representations of logical expressions from data annotated in different languages. It further investigates how such learned representations which contian shared information cross different languages can be used for improving the performance of a monolingual semantic parser.

## Installation

Java 1.8

```
sudo add-apt-repository ppa:webupd8team/java
sudo apt-get update
sudo apt-get install oracle-java8-installer
```

ZeroMQ

```
git clone https://github.com/zeromq/libzmq
cd libzmq
./autogen.sh && ./configure && make -j 4
make check && sudo make install && sudo ldconfig
```

JZMQ

```
git clone https://github.com/zeromq/jzmq
cd jzmq/jzmq-jni
./autogen.sh && ./configure && make
sudo make install
```

Torch dependency libraries

```
luarocks install lzmq
luarocks install lua-messagepack
luarocks install luautf8
```

Swipl (for evaluation)

```
sudo apt install swi-prolog-nox
```

A jar file in provided in `bin/`. One easy way to re-compile the code is to create a new project in Eclipse and export a runnable JAR file for the main class: `SemTextExperimenter_Discriminative`.

## Word embedding

Download the .pkl files from [Polyglot]( https://sites.google.com/site/rmyeid/projects/polyglot#TOC-Download-the-Embeddings). Put these files in `neural_server/polyglot`, then run the following to preprocess for Torch:

```
bash prepare_torch.sh
```
## Reproducing the experimental results

First, pretrain the discriminative hybrid tree model [(Lu, 2015)](http://www.statnlp.org/paper/constrained-semantic-forests-for-improved-discriminative-semantic-parsing.html) with cross-lingual output features for the target languages specified by <langN>:

```
bash embedding_pretrain_run.sh <lang1> <lang2> ... <langN>
```

Run a neural net server that listens on port 5556 and specify the `gpuid` (>= 0 for GPU, -1 for CPU)

```
th server.lua -port 5556 -gpuid -1
```

Train the neural hybrid tree model [(Susanto and Lu, 2017)](http://www.statnlp.org/paper/semantic-parsing-with-neural-hybrid-trees.html) with cross-lingual output features for the target languages specified by <langN>:

```
bash embedding_neural_run.sh <lang1> <lang2> ... <langN>
```


## Cite 
```
@InProceedings{yanyan-18-cross,
  author    = {Yanyan, Zou and Wei, Lu},
  title     = {Learning Cross-lingual Distributed Logical Representations for Semantic Parsing},
  booktitle = {Proceedings of the 56th Annual Meeting of the Association for Computational Linguistics},
  month     = {July},
  year      = {2018},
  address   = {Melbourne, Australia},
  publisher = {Association for Computational Linguistics},
}
```
```

## Contact

Yanyan Zou and Wei Lu, Singapore University of Technology and Design

Please feel free to drop an email at yanyan_zou@mymail.sutd.edu.sg for questions.
