<?php

function db_connect() {
	$spoj = ($GLOBALS["mysqli_spoj"] = mysqli_connect("localhost",  "login",  "pswd"));
	mysqli_select_db($GLOBALS["mysqli_spoj"], "schema");
	mysqli_query($GLOBALS["mysqli_spoj"], "set character set UTF8"); 
  mysqli_query($GLOBALS["mysqli_spoj"], "set names UTF8"); 
  mysqli_query($GLOBALS["mysqli_spoj"], "charset UTF8"); 
  mysqli_query($GLOBALS["mysqli_spoj"], "SET time_zone='+01:00'"); 
	if (!$spoj): $error=1; echo "An error ocurred while connecting into database. Please contact an administrator."; die(''); endif;
}

function db_close() {
	mysqli_close($GLOBALS["mysqli_spoj"]);
}

?>