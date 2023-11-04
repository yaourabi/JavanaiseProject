# Jananaise
To test the project, run JvnCoordImpl then Irc1 for javanaise 1 and Irc2 for javanaise 2.
# Extensions
- Cache management mechanism: makes use of LinkedHashMap to remove the eldest entries. It removes jvnObjects elements
  in JvnServerImpl. 
- Coordinator backup: saves data in the file coordObject.txt if the coordinator crashes.