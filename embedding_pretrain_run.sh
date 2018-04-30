set -e -x
mkdir -p logs model

thread=16
iter=150

for lang in $@; do
	extra_flags="-sequential-touch"
	if [ $lang = "en" ]; then
		extra_flags=" -extract-from-test"
		add=de,el,th,zh,id,sv,fa
		shapesize=30
	fi

	if [ $lang = "de" ]; then
		shapesize=30
		add=en,el,th,zh,id,sv,fa
	fi

	if [ $lang = "el" ]; then
		shapesize=10
		add=en,de,th,zh,id,sv,fa
	fi

	if [ $lang = "th" ]; then
		shapesize=20
		add=en,de,el,zh,id,sv,fa
	fi
	if [ $lang = "zh" ]; then
		shapesize=10
		add=en,de,el,th,id,sv,fa
	fi

	if [ $lang = "id" ]; then
		shapesize=30
		add=en,de,el,th,zh,sv,fa
	fi

	if [ $lang = "sv" ]; then
		shapesize=20
		add=en,de,el,th,zh,id,fa
	fi

	if [ $lang = "fa" ]; then
		shapesize=10
		add=en,de,el,th,zh,id,sv
	fi
	prefix=truncated-embedding-$lang-shapesize$shapesize
	time java -Xmx450g -Djava.library.path=/usr/local/lib -cp lib/json-20140107.jar:lib/zmq.jar:lib/junit-4.11.jar:lib/hamcrest-core-1.3.jar:lib/msgpack-core-0.8.8.jar:lib/commons-math3-3.6.1.jar:bin/sp_embedding.jar com.statnlp.example.sp.main.SemTextExperimenter_Discriminative_Embedding -thread $thread -lang $lang -optim lbfgs -l2 0.01 -iter $iter -save-iter $iter -output-embedding -add-lang $add -shapesize $shapesize -save-prefix $prefix $extra_flags > logs/$prefix.$lang.out 2> logs/$prefix.$lang.err
	done
done


