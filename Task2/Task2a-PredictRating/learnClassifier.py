# This method learns the classifiers based on the feature vectors
# created in another file

from sklearn import metrics
from sklearn.svm import LinearSVC
from sklearn.naive_bayes import MultinomialNB 
from sklearn.multiclass import OneVsRestClassifier 
from sklearn import cross_validation
from sklearn.feature_selection import RFE
import pickle
from pprint import pprint
from time import time
import sys

def main():
    
    with open("/Users/huzefa/Workspace/College-Fall-2015/Search/Dataset/Task3/reviews2not.fv", 'rb') as f:
        global XAll
        XAll= pickle.load(f)
    with open("/Users/huzefa/Workspace/College-Fall-2015/Search/Dataset/Task3/reviews2.target2", 'rb') as f:
        global yAll 
        yAll= pickle.load(f)
    with open("/Users/huzefa/Workspace/College-Fall-2015/Search/Dataset/Task3/reviews2.mlb", 'rb') as f:
        mlb = pickle.load(f)
    
    xTrain, xTest, yTrain, yTest = cross_validation.train_test_split(XAll, yAll, test_size=0.4)
    
    ############################################### SVC #############################################
    classifierTime = time()
    print "Training Classifier"
    classifier = LinearSVC()
    #classifier = LinearSVC()
    rfe = OneVsRestClassifier(RFE(classifier, step=0.9)).fit(xTrain, yTrain)
    print "Training classifier took : "+str(time()- classifierTime)
    with open("/Users/huzefa/Workspace/College-Fall-2015/Search/Dataset/Task3/LinearSVC60RFEnot.classifier", 'wb') as f:
        pickle.dump(rfe, f)
    print "Classifier dumped on disk"    
    print "Predicting ... "
    predicted = rfe.predict(xTest)
    with open("/Users/huzefa/Workspace/College-Fall-2015/Search/Dataset/Task3/Results/LinearSVC40TestRFEnot.Reviews", 'w') as f:
        f.write(metrics.classification_report(yTest, predicted))
    ##################################################################################################
    
    ############################################### MultinomialNB #############################################
    classifierTime = time()
    print "Training Classifier"
    classifier = OneVsRestClassifier(MultinomialNB())
    rfe = classifier.fit(xTrain, yTrain)
    #classifier = MultinomialNB().fit(xTrain, yTrain)
    print "Training classifier took : "+str(time()- classifierTime)
    with open("/Users/huzefa/Workspace/College-Fall-2015/Search/Dataset/Task3/Multinomial60not.classifier", 'wb') as f:
        pickle.dump(rfe, f)
    print "Classifier dumped on disk"    
    print "Predicting ... "
    predicted = rfe.predict(xTest)
    with open("/Users/huzefa/Workspace/College-Fall-2015/Search/Dataset/Task3/Results/Multinomial40TestRFEnot.Reviews", 'w') as f:
        f.write(metrics.classification_report(yTest, predicted))
    ##################################################################################################
    
if __name__ == "__main__": main()