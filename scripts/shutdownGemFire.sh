#!/usr/bin/env bash


gfsh -e "connect --locator=localhost[10334]"  -e "shutdown --include-locators=true --time-out=15"
