<?php
$f = file_get_contents("php://stdin");
$a = str_split($f, 2);
$array = array();

foreach ($a as $value) {
    $array["00000000" . $value] = 0;
}
/* $a=explode(":", $f); */
// $array = array();

// foreach ($a as $value) {
    // $array[$value] = 0;
/* } */
// var_dump($array);
printf("%d\n", count(arpegio_array_collision_info($array)[0]));
?>
