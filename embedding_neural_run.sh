set -e -x
mkdir -p logs model
debug=""
thread=16
saveiter=150
optim=adam
lr=0.001
config="-config neural_server/neural.sp.config"
hidden=100
num_layer=1
L2=0.01

emb=polyglot
embsize=64

dropout=0.0
fixpre="-fix-pretrain"
fixemb=""
iter=150
for lang in $@; do
	for window in 0 1 2 ; do
		
		if [ $lang = "en" ]; then
			add=de,el,th,zh,id,sv,fa
			shapesize=30
		fi

		if [ $lang = "de" ]; then
			add=en,el,th,zh,id,sv,fa
			shapesize=30
		fi

		if [ $lang = "el" ]; then
			add=en,de,th,zh,id,sv,fa
			shapesize=10
		fi

		if [ $lang = "th" ]; then
			add=en,de,el,zh,id,sv,fa
			shapesize=20
		fi
		if [ $lang = "zh" ]; then
			add=de,el,th,en,id,sv,fa
			shapesize=10
		fi

		if [ $lang = "id" ]; then
			add=de,el,th,zh,en,sv,fa
			shapesize=30
		fi

		if [ $lang = "sv" ]; then
			add=de,el,th,zh,id,en,fa
			shapesize=20
		fi

		if [ $lang = "fa" ]; then
			add=de,el,th,zh,id,sv,en
			shapesize=10
		fi		
		pretrain="-pretrain model/truncated-embedding-$lang-shapesize$shapesize.$lang.149.model"
  		prefix=truncated-$lang-shapeSize$shapesize-window$window
  		time java -Xmx400g -Djava.library.path=/usr/local/lib -cp lib/json-20140107.jar:lib/zmq.jar:lib/junit-4.11.jar:lib/hamcrest-core-1.3.jar:lib/msgpack-core-0.8.8.jar:lib/commons-math3-3.6.1.jar:bin/sp_embedding.jar com.statnlp.example.sp.main.SemTextExperimenter_Discriminative_Embedding -thread 16 $config -lang $lang -neural -optim $optim -lr $lr -l2 $L2 -iter $iter -save-iter $iter  -save-prefix $prefix  -num-layer $num_layer -hidden $hidden -embedding $emb -embedding-size $embsize -window $window -dropout $dropout $pretrain $fixpre > logs/$prefix.out 2> logs/$prefix.err

  	done
done
