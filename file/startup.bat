@echo off
::chcp - Change Code Page（更改代码页）命令 ;  65001 - UTF-8 的代码页编号  ;  > nul - 将命令输出重定向到空设备（不显示在控制台）
chcp 65001 > nul
::指定端口号
title 8188
java -Dfile.encoding=UTF-8 ^
     -Dsun.stdout.encoding=UTF-8 ^
     -Dsun.stderr.encoding=UTF-8 ^
     -server ^
	 -Xms4096m -Xmx4096m ^
	 -jar fancky-0.0.1-SNAPSHOT.jar ^
	 --server.port=8188 ^
	 --spring.config.location=file:application.yml ^
	 --spring.redis.redisson.config=file:./redisson-config.yml ^
	 --logging.config=file:./log4j2.xml ^
	 ::Windows 批处理文件 中，必须保证每一行续行都以 ^ 结尾，除了最后一行。
	 ::jar 内的
	 ::--logging.config=classpath:log4j2.xml
	 ::外部配置文件
	 ::--logging.config=file:./log4j2.xml
::pause