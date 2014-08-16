if [ "$1" == "" ]; then
	echo Usage: install_databook.sh download_dir databook_dir create_db compile_vivo indexing_jar proton_jar
	exit
fi
compile_vivo=$3
create_db=$4
pwd=`pwd`
echo working dir $pwd
downloads_dir=$pwd/$1
databook_dir=$pwd/$2
indexing_jar=index-0.0.1-SNAPSHOT.jar
indexing_url=https://raw.githubusercontent.com/DICE-UNC/indexing-irods/master/install.sh
if [ "$5" == "" ]; then
	indexing_jar_path=/var/lib/irods/indexing/indexing/target/$indexing_jar
	qpid_jar_path=/var/lib/irods/indexing/qpid-proton-0.7/build/proton-j/proton-j-0.7.jar
else
	indexing_jar_path=$5
	qpid_jar_path=$6
fi
tomcat_dir=/var/lib/tomcat7
vivo_dir=$databook_dir/vivo-rel-1.5
lib_dir=$vivo_dir/vitro-core/webapp/lib
data_dir=$databook_dir/data
vivo_arc=vivo-rel-1.5.tar.gz
vivo_url="http://downloads.sourceforge.net/project/vivo/VIVO%20Application%20Source/vivo-rel-1.5.tar.gz"
vivo_root=root@localhost
dbname=vivo_databook
vivo_webapp_name=vivo
mysql_username=vivo_databook
mysql_password=`< /dev/urandom tr -dc _A-Z-a-z-0-9 | head -c32`
echo generated password $mysql_password
databook_root=root@databook
echo please enter your mysql root password:
read mysql_root_password

if [ "$5" == "" ]; then
	# install indexing framework
	pushd /var/lib/irods
	sudo wget $indexing_url
	sudo bash install.sh
	sudo chown -R irods:irods indexing
	popd
fi

# stop tomcat

sudo service tomcat7 stop

# mkdirs

mkdir -p $downloads_dir
mkdir -p $databook_dir
mkdir -p $data_dir

if [ -e $downloads_dir/$vivo_arc ]; then
	echo
else
	pushd $downloads_dir
	echo wget from $vivo_url
	wget $vivo_url
	popd
fi

pushd $databook_dir
tar zxvf $downloads_dir/$vivo_arc
popd

# install & setup mysql

sudo apt-get install -y git mysql-server mysql-client openjdk-6-jdk gnome-icon-theme-full ant tomcat7

if [ "$create_db" == "1" ]; then
mysql -u root -p"$mysql_root_password" -e "DROP USER '$mysql_username'@'localhost';"
mysql -u root -p"$mysql_root_password" -e "DROP DATABASE $dbname;"
mysql -u root -p"$mysql_root_password" -e "CREATE USER '$mysql_username'@'localhost' IDENTIFIED BY '$mysql_password';"
mysql -u root -p"$mysql_root_password" -e "CREATE DATABASE $dbname CHARACTER SET utf8;"
mysql -u root -p"$mysql_root_password" -e "GRANT ALL ON $dbname.* TO '$mysql_username'@'localhost' IDENTIFIED BY '$mysql_password';"
fi

# setup indexing
cp $indexing_jar_path $qpid_jar_path $lib_dir
pushd $lib_dir
unzip -n $indexing_jar \*.jar 
popd

# setup vivo
pushd $vivo_dir
if [ "$create_db" == "1" ]; then
cp example.deploy.properties deploy.properties
sed -i \
 -e 's#^\(Vitro\.defaultNamespace\s*=\s*\).*$#\1http://datafed.org/individual/#' \
 -e 's#^\(tomcat\.home\s*=\s*\).*$#\1'$tomcat_dir'#' \
 -e 's#^\(webapp\.name\s*=\s*\).*$#\1'$vivo_webapp_name'#' \
 -e 's#^\(vitro\.home\.directory\s*=\s*\).*$#\1'$data_dir'#' \
 -e 's#^\(VitroConnection\.DataSource\.url\s*=\s*\).*$#\1jdbc:mysql://localhost/'$dbname'#' \
 -e 's#^\(VitroConnection\.DataSource\.username\s*=\s*\).*$#\1'$mysql_username'#' \
 -e 's#^\(VitroConnection\.DataSource\.password\s*=\s*\).*$#\1'$mysql_password'#' \
 -e 's#^\(rootUser\.emailAddress\s*=\s*\).*$#\1'$vivo_root'#' \
 -e 's#^\(vitro.local.solr.url\s*=\s*\).*$#\1http://localhost:8080/'$vivo_webapp_name'solr#' \
 deploy.properties
fi

if [ "$compile_vivo" == "1" ]; then
	sudo ant all
fi
popd

# setup databook
pushd $databook_dir
git clone https://github.com/DICE-UNC/vivo.git
cd vivo
git stash
git pull
sed -i \
 -e 's#^\(def downloads_dir\s*=\s*\).*$#\1\"'$downloads_dir'\"#' \
 -e 's#^\(def databook_dir\s*=\s*\).*$#\1\"'$databook_dir'\"#' \
 build.gradle
sudo gradle -q compile
sudo chown -R tomcat7:tomcat7 $tomcat_dir/webapps
sudo chown -R tomcat7:tomcat7 $data_dir
popd

# start tomcat
sudo service tomcat7 start
