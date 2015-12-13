# This file creates feature vectors for the learnClassifier.py file

from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.preprocessing import MultiLabelBinarizer
import pickle
from nltk.stem.wordnet import WordNetLemmatizer
import string
from nltk.corpus import stopwords
from nltk.tokenize import TweetTokenizer
from time import time
from pprint import pprint
from multiprocessing import Pool
import itertools
import sys 
import numpy as np

def countCapitalLetters(s):
    count = 0
    for char in s:
        if char.isupper():
            count += 1
    return count

def countExclamationMarks(s):
    count = 0
    for char in s:
        if char == "!":
            count +=1
    return count
    
def negateWords(s):
    s = s.replace("not ", "not")
    return s

def removeStopwords(wordlist):
    customStopwords = set(stopwords.words('english')) - set(['not'])
    cleanList = [word for word in wordlist if word not in customStopwords]
    return cleanList
    
def removePunctuation(s):
    punct = set(string.punctuation)
    return ''.join(ch for ch in s if ch not in punct)

def cleanReview(review):
    lemmatizer = WordNetLemmatizer()
    tokenizer = TweetTokenizer()
    #Skip empty lines
    if review in ['\n', '\r\n']:
        return ""
    line = review.lower() # Task 1
    tokens = tokenizer.tokenize(line) #Task 3
    lemmaTokens = [lemmatizer.lemmatize(token) for token in tokens]  #Task 4
    stopwordsRemoved = removeStopwords(lemmaTokens) #Task 8
    cleanTokens = [removePunctuation(word) for word in stopwordsRemoved]
    s = ""
    for word in cleanTokens:
        s= s+word+" "
    #return negateWords(s) #Task 2s
    return s
def DictToList(reviewDict):
    print "Creating corpus ... "
    startTime = time()
    reviewCorpus = []
    rating = []    
    for key, value in reviewDict.iteritems():
        reviewCorpus.append(cleanReview(key))
        rating.append(value)
    print "Corpus created in "+str(time() - startTime)
    return (reviewCorpus, rating)

def cleanSingleReviewFromKey(key, reviewsDict):
    review = cleanReview(key)
    rating = str(reviewsDict[key])
    
    return review, rating
    
def main():
    #Explore the data for how many class labels
    global reviewsDict
    reviewsDict = {}
    with open("/Users/huzefa/Workspace/College-Fall-2015/Search/Dataset/Task3/reviewRatingDict.pickle") as f:
        reviewsDict = pickle.load(f)
    print "Reviews Dictionary loaded .. "
    #p = Pool(10)
    #key = reviewsDict.keys()
    #corpus, target = p.map(cleanSingleReviewFromKey, itertools.izip(key, itertools.repeat(reviewsDict)))
    corpus, target = DictToList(reviewsDict)
    '''
    with open("/Users/huzefa/Workspace/College-Fall-2015/Search/Dataset/Task3/reviews2.targetnumeric", 'wb') as f:
        pickle.dump(target, f)
    
    with open("/Users/huzefa/Workspace/College-Fall-2015/Search/Dataset/Task3/reviews2.corpus", 'wb') as f:
        pickle.dump(corpus, f)
    '''
    
    vectorizer = TfidfVectorizer(stop_words="english", min_df=1)
    
    XAll = vectorizer.fit_transform(corpus)
    #mlb = MultiLabelBinarizer()
    #yAll = mlb.fit_transform(target)
    
    with open("/Users/huzefa/Workspace/College-Fall-2015/Search/Dataset/Task3/reviews2not.fv", 'wb') as f:
        pickle.dump(XAll, f)
    #with open("/Users/huzefa/Workspace/College-Fall-2015/Search/Dataset/Task3/reviews2.target2", 'wb') as f:
    #    pickle.dump(yAll, f)
    #with open("/Users/huzefa/Workspace/College-Fall-2015/Search/Dataset/Task3/reviews2.mlb", 'wb') as f:
    #    pickle.dump(mlb, f)
    
    print "Dumped feature vectors .... "
    
if __name__ == "__main__": main()