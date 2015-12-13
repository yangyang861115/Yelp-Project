import pickle
import json

def main():
    '''
    To reduce the overall memory usage at once and enable reusability, 
    a file is read & an appropriate datastructure is applied, then the 
    data strucutre is serialized and dumped on disk. This can be utilized in 
    multiple ways to create feature vectors
    '''
    
    print "Started reading file ... "
    reviewUsefulDict = {}
    with open("/Users/huzefa/Workspace/College-Fall-2015/Search/Dataset/yelp_academic_dataset_review.json") as f:
        for line in f:
            parsedJSON = json.loads(line.decode('utf8'))
            # Review is the key and usefulness rating is value
            reviewUsefulDict[parsedJSON["text"]] = parsedJSON["votes"]["useful"]
    print "Read file successfully ... "
    print "Pickling data to disk .. "
    with open("/Users/huzefa/Workspace/College-Fall-2015/Search/Dataset/reviewUsefulDict.pickle", 'w') as picklefile:    
        pickle.dump(reviewUsefulDict,picklefile)
    print "File dumped on disk ..."
    
if __name__ == "__main__": main()