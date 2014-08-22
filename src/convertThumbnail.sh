#!/bin/sh
convert -define jpeg:size=200x200 $1 -thumbnail '200x200' $3
convert -define jpeg:size=800x600 $1 -resize '800x600' $5
iput -f $3 $2
iput -f $5 $4
