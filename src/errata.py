__author__ = 'Akshay'
import sys
from os import listdir
from os.path import isfile, join
import os
import matplotlib.pyplot as plt
import numpy as np
from sklearn import datasets, linear_model
def clear1():
    mypath = os.path.abspath("result/")
    onlyfiles = [ f for f in listdir(mypath) if isfile(join(mypath,f)) ]
    filter = ["[","=1","]:","<conf:(",")",">",","]
    output = []
    for fl in onlyfiles:
        fl = mypath+"/"+fl
        with open(fl, "rb") as f:
            for line in f.readlines():
                line = line.split()
                temp = []
                for element in line:
                    if '<' in element or '_' in element:
                        for val in filter:
                            if val in element:
                                element = element.replace(val,'')
                        temp.append(element)
                output.append(temp)
        with open(fl,"wb") as f:
            for line in output:
                for ele in line:
                    f.write(ele)
                    f.write(" ")
                f.write("\n")

def clear2():
    index = []
    with open("schema") as f:
        for line in f.readlines():
            line = line.split()
            index.append(line[1])


    mypath = os.path.abspath("result/")
    onlyfiles = [ f for f in listdir(mypath) if isfile(join(mypath,f)) ]
    output = []
    for fl in onlyfiles:
        fl = mypath+"/"+fl
        with open(fl, "rb") as f:
            for line in f.readlines():
                line = line.split()
                temp = []
                for element in line:
                    try:
                        element = str(index.index(element))
                    except ValueError:
                        element = element
                    temp.append(element)
                output.append(temp)
        with open(fl,"wb") as f:
            for line in output:
                for ele in line:
                    f.write(ele)
                    f.write(" ")
                f.write("\n")