#!/bin/bash

# アップデートディレクトリ
UPDATE="/home/kuro/mcbe_update/"
# 本ディレクトリ
EXECUTE="/home/kuro/mcbe/"

# アップデートディレクトリ内のファイルはそのまま上書いて削除
COUNT=$(find $UPDATE -type f |wc -l)
if [ $COUNT > 0 ]; then
        rsync -av $UPDATE $EXECUTE
	find $UPDATE -type f -name "*.*" -delete
fi

while :
do
	# Nukkit実行
	java -Duser.timezone=Asia/Tokyo -Djline.terminal=jline.UnsupportedTerminal -Xms1024m -Xmx1560m -XX:-UseCodeCacheFlushing -XX:ReservedCodeCacheSize=384m -XX:PermSize=256m -XX:MaxPermSize=256m -XX:NewSize=128m -XX:MaxNewSize=256m -XX:NewRatio=2 -XX:SurvivorRatio=8 -XX:TargetSurvivorRatio=80 -XX:-UseGCOverheadLimit -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=./ -jar nukkit-1.0-SNAPSHOT.jar

	# アップデートディレクトリ内のファイルはそのまま上書いて削除
	COUNT=$(find $UPDATE -type f |wc -l)
	if [ $COUNT > 0 ]; then
		rsync -av $UPDATE $EXECUTE
		find $UPDATE -type f -name "*.*" -delete
		CONTINUE
	fi
	# アップデートがないなら落ちる
	break
done
