# 🕸️ Facebook 2.0

Once while I was wondering why we exist during solving my final exam, I read an interesting question - which is an unlikely event to occur - it was a software architecture design question saying (not a word-to-word quoting):

> If we want to design a fully distributed version of Facebook, that is instead of having a ~30k server farm to run the different services, we want to design a P2P distributed architecture modeling each user phone as a node in the P2P network instead of the server farm.

The question also suggested using a [__Distributed Hash Table (DHT)__](https://en.wikipedia.org/wiki/Distributed_hash_table) to model the distributed network and only asked to propose a design to implement the _registration/login_ & _create/view posts_ (wall app in Facebook) services. Next, I will discuss how I approached this design question and I will follow my thinking process back then so there might be very naive approaches 😅.

## Distributed Hash Table
From the name, we can guess what a DHT is. Basically, it's a distributed data structure meaning it lives across different nodes/machines and it supports two operations storing a key-value pair and given a key retrieve its corresponding value. The aim of the different DHT designs is to store a huge amount of data that one single machine can't handle while supporting the characteristics of any distributed datastore which are: Scalability, Reliability, Fault Tolerance, and Performance. The first DHT that came to my mind was [__Chord__](https://pdos.csail.mit.edu/papers/chord:sigcomm01/chord_sigcomm.pdf). Chord is a very efficient and well-designed DHT, it utilizes consistent hashing and circular id space to structure the network of nodes in a way that enables _O(log(N))_ retrieval of any data, _N_ here represents the number of the nodes in the network. It's also resilient in the presence of churn (churn happens when there are some nodes joining and leaving the network in a random manner) thanks to its stabilization algorithm. This isn't a post about how Chord work, but you can read the [__paper__](https://pdos.csail.mit.edu/papers/chord:sigcomm01/chord_sigcomm.pdf) for more in-depth design details.


## 🔑 Registration/Login
Registration can be done as a broadcast operation, where each new user sends his/her registration information to all the nodes in the network to store it and use it later for authentication. Note that this is a broadcast operation, not a simple store operation, we will discuss why later.

Then to model Loggin in, the network needs to agree on the validity of given login data so to achieve that we can use [__Consesue Protocols__](https://en.wikipedia.org/wiki/Consensus_(computer_science)) the proof here would be the password (or any unique data that other nodes can verify) and the consensus could be done with the majority voting _(we can get very creative here - and avoid all of the security issues that probably came to your mind right now - and use [__Zero-knowledge proof__](https://en.wikipedia.org/wiki/Zero-knowledge_proof))_

But this approach is very dumb - probably as you are currently thinking - and that's at least for the following three reasons:
1. Each node in the network will store each other user information, meaning it has a full copy of the login & registration database, which is not scalable.
2. In order for a user to log in, we need the consent of at least 51% of the network to verify his/her login info, and that is very slow (this is not the case for other consensus protocols).
3. The killer issue is if we tried to think about what P2P means, we will see that there is no notion of privately stored data, all data is by definition accessible to all peers so storing passwords and verifying their hash prove nothing, we need another way to model authentication.

To solve the killer issue, we need to go one step up in the semantics layer, why do we need to implement login and registration? the answer would be that we need to identify users in our system in order to authorize actions taken! So what about authorizing each user by having each user compute a hash of (public IP, local port, some nonce) using __SHA-1__ for example, and use this hash as an _ID_ to join Chord network (info: chord identifies different machines by 128-bit id in the id space "Ring"). By implementing this simple approach we don't even need the registration as each node identifies itself with a unique hash. 


## 🗞️ Wall App
Creating new posts is very straightforward, we can model it as storing a key (128-bit UUID of the post) and value (post itself) in the network. This can be done using the store algorithm of Chord ([see paper](https://pdos.csail.mit.edu/papers/chord:sigcomm01/chord_sigcomm.pdf)).

But implementing the Wall feed itself is a bit tricky. We can go the naive way and generate some random 128-bit hashes and retrieve the posts corresponding to these hashes (or the nearest post if one does not exist) but that would be considered lazy and does not fully implement the recommendation features of the wall service.

> Note: Chord works by mapping both nodes and keys (from key-value pairs) to the same circular space, and then each node is responsible for all the pairs whose keys follow the node till the next node in the Ring, __but__ there are other DHTs that uses a cartesian space as a way to model nodes and keys.

Trying to satisfy these constraints of getting a feed that is actually relevant would require a bit of knowledge about machine learning, and also implementing a distributed recommendation system. My proposal was first we need to use some pre-trained neural network model and by using [__transfer learning__](https://en.wikipedia.org/wiki/Transfer_learning) trick we can use the last but one layer as an embedding vector that represents the post in the features space. Then we need to maintain another d-dimensional cartesian space based DHT like [__CAN__](http://conferences.sigcomm.org/sigcomm/2001/p13-ratnasamy.pdf) to store post embeddings generated by the ML model. The last thing we need to have is some embedding vector that represents user interests (could be calculated based on his own posts, reacted-with-posts, friends-posts, etc.) and by using this vector to search for the nearest-K neighbors in the CAN network we have implemented the feed with distributed recommendation system 🎉.

> Disclaimer: things here could be hardly implementable or not applicable at all, but if you feel that there are some things that are overly stupid and/or wrong feel free to contact me 👍.

