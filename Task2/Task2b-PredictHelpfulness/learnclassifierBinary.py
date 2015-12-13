#This file learns the classifiers and produces results
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.svm import LinearSVC
from sklearn.naive_bayes import MultinomialNB 
from sklearn.multiclass import OneVsRestClassifier 
from sklearn.preprocessing import MultiLabelBinarizer
from sklearn import cross_validation
from sklearn import metrics
from sklearn.linear_model import SGDClassifier

import pickle
from pprint import pprint
from time import time

def main():
    
    with open("/Users/huzefa/Workspace/College-Fall-2015/Search/Dataset/Task2/Onlyreviews.fv") as f:
        global XAll
        XAll= pickle.load(f)
    with open("/Users/huzefa/Workspace/College-Fall-2015/Search/Dataset/Task2/Onlyreviews.target2") as f:
        global yAll 
        yAll= pickle.load(f)
    with open("/Users/huzefa/Workspace/College-Fall-2015/Search/Dataset/Task2/Onlyreviews.mlb") as f:
        mlb = pickle.load(f)
    
    xTrain, xTest, yTrain, yTest = cross_validation.train_test_split(XAll, yAll, test_size=0.4)
    ############################################### SVC #############################################
    classifierTime = time()
    print "Training Classifier"
    #svcClassifier = OneVsRestClassifier(LinearSVC()).fit(xTrain, yTrain)
    sgdClassifier = OneVsRestClassifier(SGDClassifier()).fit(xTrain, yTrain)
    print "Training classifier took : "+str(time()- classifierTime)
    with open("/Users/huzefa/Workspace/College-Fall-2015/Search/Dataset/Task2.1/sgd60Binary.classifier", 'w') as f:
        pickle.dump(sgdClassifier, f)
    print "Classifier dumped on disk"    
    print "Predicting ... "
    predicted = sgdClassifier.predict(xTest)
    with open("/Users/huzefa/Workspace/College-Fall-2015/Search/Dataset/Task2.1/Results/sgd40Test.Reviews", 'w') as f:
        f.write(metrics.classification_report(yTest, predicted, target_names=mlb.classes_))
    ##################################################################################################
    
    xTrain, xTest, yTrain, yTest = cross_validation.train_test_split(XAll, yAll, test_size=0.4)
    ############################################### SVC #############################################
    classifierTime = time()
    print "Training Classifier"
    #svcClassifier = OneVsRestClassifier(LinearSVC()).fit(xTrain, yTrain)
    sgdClassifier = OneVsRestClassifier(MultinomialNB()).fit(xTrain, yTrain)
    print "Training classifier took : "+str(time()- classifierTime)
    with open("/Users/huzefa/Workspace/College-Fall-2015/Search/Dataset/Task2.1/multinomialNB60Binary.classifier", 'w') as f:
        pickle.dump(sgdClassifier, f)
    print "Classifier dumped on disk"    
    print "Predicting ... "
    predicted = sgdClassifier.predict(xTest)
    with open("/Users/huzefa/Workspace/College-Fall-2015/Search/Dataset/Task2.1/Results/multinomialNB40Test.Reviews", 'w') as f:
        f.write(metrics.classification_report(yTest, predicted, target_names=mlb.classes_))
    ##################################################################################################
if __name__ == "__main__": main()