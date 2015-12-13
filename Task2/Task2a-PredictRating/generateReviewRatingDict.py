# This file creates a dictionary of reviews and ratings which will be used to create 
# feature vectors

import pickle
import json
from multiprocessing import Process

def createReviewRatingDict(rootDir):
    print "Started reading reviews file ... "
    reviewUsefulDict = {}
    with open(rootDir+"yelp_academic_dataset_review.json") as f:
        for line in f:
            parsedJSON = json.loads(line.decode('utf8'))
            # Review is the key and usefulness rating is value
            reviewUsefulDict[parsedJSON["text"]] = parsedJSON["stars"]
    print "Read file successfully ... "
    print "Pickling data to disk .. "
    with open(rootDir+"reviewRatingDict.pickle", 'wb') as picklefile:    
        pickle.dump(reviewUsefulDict,picklefile)
    print "Review Rating Dict dumped on disk ..."
    return reviewUsefulDict
    
def main():
    rootDir = "/N/u/hydargah/BigRed2/search/RatingTask/"
    #rootDir = "/Users/huzefa/Workspace/College-Fall-2015/Search/Dataset/"
    
    #Create reviewRatingDictionary 
    reviewDict = createReviewRatingDict(rootDir)
    
    
if __name__ == "__main__": main()