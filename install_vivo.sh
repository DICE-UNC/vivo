pwd=`pwd`
downloads_dir=$pwd/$1
databook_dir=$pwd/$2
indexing_url=https://raw.githubusercontent.com/DICE-UNC/indexing-irods/master/install.sh
if [ "$3" == "" ]; then
	indexing_jar_path=/var/lib/irods/indexing/indexing/target/index-0.0.1-SNAPSHOT.jar
else
	indexing_jar_path=$3
fi
tomcat_dir=/var/lib/tomcat7
vivo_dir=$databook_dir/vivo-rel-1.5
lib_dir=$vivo_dir/lib
data_dir=$databook_dir/data
vivo_arc=vivo-rel-1.5.tar.gz
vivo_url="http://downloads.sourceforge.net/project/vivo/VIVO%20Application%20Source/vivo-rel-1.5.tar.gz"
dbname=vivo_databook
mysql_username=vivo_databook
mysql_password=`< /dev/urandom tr -dc _A-Z-a-z-0-9 | head -c32`
echo generated password $mysql_password
databook_root=root@databook
echo please enter your mysql root password:
read mysql_root_password

if [ "$3" == "" ]; then
	# install indexing framework
	pushd /var/lib/irods
	wget $indeixng_url
	chmod install.sh
	sudo ./install.sh
	sudo chown -R irods:irods indexing
	popd
fi

# mkdirs

mkdir -p downloads_dir
mkdir -p databook_dir

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

sudo apt-get install -y git mysql-server mysql-client openjdk-6-jdk gnome-icon-theme-full ant tomcat7 gradle

mysql -u root -p"$mysql_root_password" -e "DROP USER '$mysql_username'@'localhost';"
mysql -u root -p"$mysql_root_password" -e "DROP DATABASE $dbname;"
mysql -u root -p"$mysql_root_password" -e "CREATE USER '$mysql_username'@'localhost' IDENTIFIED BY '$mysql_password';"
mysql -u root -p"$mysql_root_password" -e "CREATE DATABASE $dbname CHARACTER SET utf8;"
mysql -u root -p"$mysql_root_password" -e "GRANT ALL ON $dbname.* TO '$mysql_username'@'localhost' IDENTIFIED BY '$mysql_password';"

# setup indexing
cp $indexing_jar_path $lib_dir

# setup vivo
pushd $vivo_dir
cp example.deploy.properties deploy.properties
sed -i \
 -e 's#^\(Vitro\.defaultNamespace\s*=\s*\).*$#\1http://datafed.org/individual/#' \
 -e 's#^\(tomcat\.home\s*=\s*\).*$#\1'$tomcat_dir'#' \
 -e 's#^\(webapp\.name\s*=\s*\).*$#\1vivo_databook#' \
 -e 's#^\(vitro\.home\.directory\s*=\s*\).*$#\1'$data_dir'#' \
 -e 's#^\(VitroConnection\.DataSource\.url\s*=\s*\).*$#\1jdbc:mysql://localhost/'$dbname'#' \
 -e 's#^\(VitroConnection\.DataSource\.username\s*=\s*\).*$#\1'$mysql_username'#' \
 -e 's#^\(VitroConnection\.DataSource\.password\s*=\s*\).*$#\1'$mysql_password'#' \
 -e 's#^\(rootUser\.emailAddress\s*=\s*\).*$#\1'$vivo_root'#' \
 deploy.properties

ant all
popd

# setup databook
pushd databook_dir
git clone https://github.com/DICE-UNC/vivo.git
cd vivo
gradle -q deploy
popd
