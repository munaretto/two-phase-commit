* iniciar instancias com flag de iniciador ou nao
* iniciador lança o vote request
* depois de alguns segundos, ele verifica as respostas:
* * Se receber pelo menos 1 VOTE_ABORT, manda uma mensagem GLOBAL_ABORT para todos
* * Se todo mundo responder VPTE_COMMIT, manda uma mensagem GLOBAL_COMMIT para todos

