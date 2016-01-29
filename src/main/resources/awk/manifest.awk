	{
	S=$0;
	i=0;
	while(length(S)>70) {
		printf("%s%s\n",(i==0?"":" "),substr(S,1,70));
		S=substr(S,71);
		i++;
		}
	printf("%s%s\n",(i==0?"":" "),S);
	}
