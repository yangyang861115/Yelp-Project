#This file creates the classifiers and makes predictions

from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.svm import LinearSVC
from sklearn.naive_bayes import MultinomialNB 
from sklearn.multiclass import OneVsRestClassifier 
from sklearn.preprocessing import MultiLabelBinarizer
from sklearn import cross_validation
from sklearn import metrics

import pickle
from pprint import pprint
from time import time
import sys

class Business:
    def __init__(self, business_id, stars, name, categories):
        self.id = business_id
        self.stars = stars
        self.name = name
        self.categories = categories
        self.cleanReviews = [] #List of list of tokenized words
        self.cleanTips = []

def createCorpus(businessDict):
    corpus = []
    target = []
    startTime = time()
    print "Started creating corpus ..." 
    for key, value in businessDict.iteritems():
        currentObj = value
        #Combining all reviews in one big string
        tipsAndReviews = ""
        tipsAll = ""
        for tip in currentObj.cleanTips:
            tipsAll += tip+" "
        reviewsAll = ""
        for review in currentObj.cleanReviews:
            reviewsAll += review+" "
        corpus.append(tipsAll+" "+reviewsAll)
        target.append(currentObj.categories)
    
    print "Corpus created in "+str(time() - startTime)
    return (corpus, target)

def main():
    startTime= time()
    businessDict = {}
    with open("/Users/huzefa/Workspace/College-Fall-2015/Search/Dataset/businessDict.pickle") as f:
        businessDict = pickle.load(f)
        print "Reading pickle completed in "+str(time() - startTime)
    XAndY = createCorpus(businessDict)
    X = XAndY[0]
    y = XAndY[1]
    
    print "Size of X : " + str(len(X))
    print "Size of y : " + str(len(y))
    
    vectorizer = TfidfVectorizer(stop_words="english", min_df=0.0009)
    XAll = vectorizer.fit_transform(X)
    mlb = MultiLabelBinarizer()
    yAll = mlb.fit_transform(y)
    
    with open("/Users/huzefa/Workspace/College-Fall-2015/Search/Dataset/XAll.processed", 'wb') as f:
        pickle.dump(XAll, f)
    
    with open("/Users/huzefa/Workspace/College-Fall-2015/Search/Dataset/YAll.processed", 'wb') as f:
        pickle.dump(yAll, f)
    print "Data Pickled "
    
    xTrain, xTest, yTrain, yTest = cross_validation.train_test_split(XAll, yAll, test_size=0.2)
    ############################################### SVC #############################################
    classifierTime = time()
    print "Training Classifier"
    svcClassifier = OneVsRestClassifier(LinearSVC()).fit(xTrain, yTrain)
    print "Training classifier took : "+str(time()- classifierTime)
    with open("/Users/huzefa/Workspace/College-Fall-2015/Search/Dataset/svc80.classifier", 'w') as f:
        pickle.dump(svcClassifier, f)
    print "Classifier dumped on disk"    
    print "Predicting ... "
    predicted = svcClassifier.predict(xTest)
    with open("/Users/huzefa/Workspace/College-Fall-2015/Search/Dataset/Task1/Results/svc20Test.ReviewsTips", 'w') as f:
        f.write(metrics.classification_report(yTest, predicted, target_names=mlb.classes_))
    ##################################################################################################
    
    xTrain, xTest, yTrain, yTest = cross_validation.train_test_split(XAll, yAll, test_size=0.4)
    ############################################### SVC #############################################
    classifierTime = time()
    print "Training Classifier"
    svcClassifier = OneVsRestClassifier(LinearSVC()).fit(xTrain, yTrain)
    print "Training classifier took : "+str(time()- classifierTime)
    with open("/Users/huzefa/Workspace/College-Fall-2015/Search/Dataset/svc60.classifier", 'w') as f:
        pickle.dump(svcClassifier, f)
    print "Classifier dumped on disk"    
    print "Predicting ... "
    predicted = svcClassifier.predict(xTest)
    with open("/Users/huzefa/Workspace/College-Fall-2015/Search/Dataset/Task1/Results/svc40Test.ReviewsTips", 'w') as f:
        f.write(metrics.classification_report(yTest, predicted, target_names=mlb.classes_))
    ##################################################################################################         
    
    xTrain, xTest, yTrain, yTest = cross_validation.train_test_split(XAll, yAll, test_size=0.4)
    ############################################### MultinomialNB #############################################
    classifierTime = time()
    print "Training Classifier"
    svcClassifier = OneVsRestClassifier(MultinomialNB()).fit(xTrain, yTrain)
    print "Training classifier took : "+str(time()- classifierTime)
    with open("/Users/huzefa/Workspace/College-Fall-2015/Search/Dataset/MultinomialNB60.classifier", 'w') as f:
        pickle.dump(svcClassifier, f)
    print "Classifier dumped on disk"    
    print "Predicting ... "
    predicted = svcClassifier.predict(xTest)
    with open("/Users/huzefa/Workspace/College-Fall-2015/Search/Dataset/Task1/Results/MNB40Test.ReviewsTips", 'w') as f:
        f.write(metrics.classification_report(yTest, predicted, target_names=mlb.classes_))
    ##################################################################################################         
    
    xTrain, xTest, yTrain, yTest = cross_validation.train_test_split(XAll, yAll, test_size=0.2)
    ############################################### MultinomialNB #############################################
    classifierTime = time()
    print "Training Classifier"
    svcClassifier = OneVsRestClassifier(MultinomialNB()).fit(xTrain, yTrain)
    print "Training classifier took : "+str(time()- classifierTime)
    with open("/Users/huzefa/Workspace/College-Fall-2015/Search/Dataset/MultinomialNB80.classifier", 'w') as f:
        pickle.dump(svcClassifier, f)
    print "Classifier dumped on disk"    
    print "Predicting ... "
    predicted = svcClassifier.predict(xTest)
    with open("/Users/huzefa/Workspace/College-Fall-2015/Search/Dataset/Task1/Results/MNB20Test.ReviewsTips", 'w') as f:
        f.write(metrics.classification_report(yTest, predicted, target_names=mlb.classes_))
    ##################################################################################################         
    
if __name__ == "__main__": main()