<?php
// $f = file_get_contents("php://stdin");
// $a=explode(":", $f);
// $array = array();
$byteArray = unpack("N*",file_get_contents("php://stdin"));
foreach ($byteArray as $value) {
    $array[$value] = 0;
}
printf("There are %d collisions at index 0\n", count(arpegio_array_collision_info($array)[0]));
?>
