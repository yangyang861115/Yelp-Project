# Creates feature vectors for the helpfulnes task
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

def DictToList(reviewDict):
    print "Creating corpus ... "
    startTime = time()
    reviewCorpus = []
    useful = []    
    for key, value in reviewDict.iteritems():
        reviewCorpus.append(key)
        if value == 0:
            useful.append(str(value))
        else:
            useful.append("1")
    
    print "Corpus created in "+str(time() - startTime)
    return (reviewCorpus, useful)

def main():
    #Explore the data for how many class labels
    reviewsDict = {}
    with open("/Users/huzefa/Workspace/College-Fall-2015/Search/Dataset/Task2/reviewUsefulDict.pickle") as f:
        reviewsDict = pickle.load(f)
    print "Reviews Dictionary loaded .. "
    '''
    usefulCountDict = {}
    for key, value in reviewsDict.iteritems():
        if value not in usefulCountDict:
            usefulCountDict[value] = 1
        else:
            usefulCountDict[value] = usefulCountDict[value]+1
    pprint(usefulCountDict)
    '''
    corpus, target = DictToList(reviewsDict)
    
    vectorizer = TfidfVectorizer(stop_words="english", max_df=0.5, sublinear_tf=True)
    XAll = vectorizer.fit_transform(corpus)
    mlb = MultiLabelBinarizer()
    yAll = mlb.fit_transform(target)
    
    with open("/Users/huzefa/Workspace/College-Fall-2015/Search/Dataset/Task2/Onlyreviews.fv", 'w') as f:
        pickle.dump(XAll, f)
    with open("/Users/huzefa/Workspace/College-Fall-2015/Search/Dataset/Task2/Onlyreviews.target2", 'w') as f:
        pickle.dump(yAll, f)
    with open("/Users/huzefa/Workspace/College-Fall-2015/Search/Dataset/Task2/Onlyreviews.mlb", 'w') as f:
        pickle.dump(mlb, f)
    
    print "Dumped featrue vectors .... "
    
if __name__ == "__main__": main()