# Space-Explorers-and-Federation-HQ
Tema 2 APD
Chirita Maria-Luissa 332CA

# CommunicationChannel
  Se foloseste cate un buffer de tipul LinkedBlockingQueue<Message> pentru
  fiecare canal: spaceExplorerChannel si hqChannel, pentru a beneficia de
  operatii atomice de put si take si de bloacarea thread-ului in cazul in
  care aceste incearca sa puna elemente intr-o coada plina sau sa ia elemente
  dintr-o coada goala.

  Pentru metodele getMessageHeadQuarterChannel, putMessageSpaceExplorerChannel
  si getMessageSpaceExplorerChannel, am folosit doar metodele put si take
  corespunzatoare buffer-ului in care scrie sau din care se citeste.

  Pentru metoda putMessageHeadQuarterChannel, nu pun pe canal toate mesajele
  primite:
	    - mesajele END sunt ignorate
	    - daca primesc EXIT, adaug mesajul pe canal.
	    - pentru ca un mesaj complet si valid despre un nod adiacent, primit de
	     un Space Explorer trebuie sa contina: parinte, nod si hash-ul nodului,
	     acesta trebuie compus in CommunicationChannel din 2 mesaje	consecutive
	     primite: mesajul cu informatii despre nodul parinte si mesajul cu
	     informatiile despre nodul adiacent. Pentru a mentine cele 2 mesaje in
	     ordine, folosesc map-ul parents in care cheia este id-ul thread-ului
	     si retin parintele atunci cand ajunge un mesaj cu informatii despre
	     acesta (atunci cand map nu contine threadId). La primirea celui de-al
	     doilea mesaj pentru acelasi thread (map contine threadId) extrag
	     parintele si creez mesajul cu el si campurile currentSolarSystem si
	     data, luate din mesajul primit.

# Space Explorer
  Thread-ul Space Explorer primeste mesaje pe canal intr-o bucla infinita.
  Daca mesajul este de tip EXIT, SpaceExplorer isi termina executia. In caz
  contrar, se trimie, pe canalul SpaceExplorer, un nou mesaj avand campurile
  parentSolarSystem si currentSolarSystem, de la mesajul initial si campul
  data pe care am aplicat functia encryptMultipleTimes.
  Se construieste mesajul nou avand campurile parentSolarSystem si
  currentSolarSystem, de la mesajul initial si campul data criptat prin
  aplicarea encryptMultipleTimes, dupa care este trimis pe canal.
