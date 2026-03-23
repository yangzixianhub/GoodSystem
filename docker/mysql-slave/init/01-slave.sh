#!/bin/bash
sleep 20
mysql -uroot -p"${MYSQL_ROOT_PASSWORD}" -e "
  CHANGE MASTER TO
    MASTER_HOST='mysql-master',
    MASTER_USER='repl',
    MASTER_PASSWORD='repl',
    MASTER_AUTO_POSITION=1;
  START SLAVE;
" 2>/dev/null || true
echo "Slave init done."
