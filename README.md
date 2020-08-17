Solution steps:

	1. Building a map that contains the word as key (for each sentence/phrase) and in the value it will contain a set of lines that contains the specific word.
	   example:
	   eache number in the set value indicates the index of the line in the file or list we got in the input.
	   Naomi: 0->1->4
	   is: 0->1->3->4
	   getting: 0->2
	   into: 0->2
	   the: 0->2
	   car: 0->2
	   eating: 1->3->4
	   at: 1->3->4
	   a: 1->3->4
	   restaurant: 1
	   George: 2->3
	   dinner: 3->4
	2. While building the map from first step we will build another map for each phrase/line that will sum up the number of common words we have with each line.
	   example (with the same exmaple we have in the task):
	   first line:
	   0-> 0
	   second line:
	   0-> 2 (means we are using 2 words from line 0)
	   1-> 4 (and we are using 4 words from the current index)
	   third line:
	   0-> 5 (means we are using 5 similar words from line 0)
	   1-> 1
	   etc...
	   from the third line we will have a match (number of words for the prashe - 1) means that the first line and third one having common template.
	   Iam calculating the total number of words from the map in the first step , so each time i find the word in the map will increment it's related lines value by 1.
	3. While running first step we will also build a map that will contain the template and the changed words as list in the value.
	   example:
	   "{0} is getting into the car" : [Naomi,George]
	   "{0} is eating at a diner" : [Naomi,George]
	then in the last step we will just replace the names in the placeholder of the template.

#Complexity:
assumin that n= number of lines , m= number of words for each line.
The solution i've implemnted depends on the number of words we have in text , building the map in first step it will take O(n*m) linear time as it depends on the words in the input only.
Linear time is good enought , but we can scale it to have better performance.

#Improvements:

In order to scale the algorithm we can use multi threads to handle the input , we can devide the input in bulks and each thread will run on it's own bulk ,
and in the result we will merge the common templates that we get from the map, but we will handle also the phrases that we didn't find a match for them in the current thread and it will be handled in other loop.

Lets assume we will have 10 threads handling 20 phrases.
so first time each thread will take 2 phrases and check if there are common template between them if not they will also return the phrases that doesn't have template.
the second time we will go over the results and have a new thread handling the results we get from two threads, means we will have 5 threads to handle.
the third time we will have 2 threads etc...

means the minimum time that we will get is (lets say t is number of threads) (n/t)*m (that in case we find all templates on the first division)
And the max complexity time that we will get ~n*m that in case we will find the templates only in the last thread.



