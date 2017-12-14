# Architecture Components

### Room

+ O banco de dados da aplicação foi todo reconstruído para utilizar os novos architeture components, o conteúdo se encontra num branch separado devido a evitar problemas de merge de ultima hora. 

+ Conseguimos perceber que o uso de Room facilita MUITO o manuseio do banco de dados. 

+ Em pouco tempo foi possivel migrar a tabela para a nova tecnologia. encontramos pequenos problemas para fazer o refatoramento, mas foram apenas relativos a migração, não ao uso em si do artifício (O aplicativo tinha sido pensado para usar o outro banco de dados, o que gerava muito código que veio a se tornar inútil). 

+ Infelizmente não tivemos tempo de implementar o Live Data por completo, por isso decidimos não adicionar o código solto.