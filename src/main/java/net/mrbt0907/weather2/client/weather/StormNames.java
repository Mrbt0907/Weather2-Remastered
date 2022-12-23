package net.mrbt0907.weather2.client.weather;

import net.mrbt0907.weather2.util.MapEX;

public class StormNames
{
	private static int used;
	public static final MapEX<String, Boolean> NAMES = new MapEX<String, Boolean>();
	
	public static void init()
	{
		used = 0;
		NAMES.clear();
		add("Agnes","Alicia","Allen","Allison","Andrew","Anita","Audrey","Betsy","Beulah","Bob","Camille","Carla","Carmen","Carol","Celia","Cesar","Charley","Cleo","Connie","David","Dean","Dennis","Diana","Diane","Donna","Dora","Dorian","Edna","Elena","Eloise","Erika","Eta","Fabian","Felix","Fifi","Flora","Florence","Floyd","Fran","Frances","Frederic","Georges","Gilbert","Gloria","Greta","Gustav","Harvey","Hattie","Hazel","Hilda","Hortense","Hugo","Ida","Igor","Ike","Inez","Ingrid","Ione","Iota","Irene","Iris","Irma","Isabel","Isidore","Ivan","Janet","Jeanne","Joan","Joaquin","Juan","Katrina","Keith","Klaus","Laura","Lenny","Lili","Luis","Maria","Marilyn","Matthew","Michael","Michelle","Mitch","Nate","Noel","Opal","Otto","Paloma","Rita","Roxanne","Sandy","Stan","Tomas","Wilma");
		add("Adele", "Adolph", "Alma", "Fefa", "Fico", "Hazel", "Ioke", "Isis", "Ismael", "Iniki", "Iva", "Iwa", "Kenna", "Knut", "Manuel", "Odile", "Paka", "Patricia", "Pauline");
		add("Alex", "Bonnie", "Colin", "Danielle", "Earl", "Fiona", "Gaston", "Hermine", "Ian", "Julia", "Karl", "Lisa", "Martin", "Nicole", "Owen", "Paula", "Richard", "Shary", "Tobias", "Virginie", "Walter", "Arlene", "Bret", "Cindy", "Don", "Emily", "Franklin", "Gert", "Harold", "Idalia", "Jose", "Katia", "Lee", "Margot", "Nigel", "Ophelia", "Philippe", "Rina", "Sean", "Tammy", "Vince", "Whitney", "Alberto", "Beryl", "Chris", "Debby", "Ernesto", "Francine", "Gordon", "Helene", "Isaac", "Joyce", "Kirk", "Leslie", "Milton", "Nadine", "Oscar", "Patty", "Rafael", "Sara", "Tony", "Valerie", "William", "Andrea", "Barry", "Chantal", "Dexter", "Erin", "Fernand", "Gabrielle", "Humberto", "Imelda", "Jerry", "Karen", "Lorenzo", "Melissa", "Nestor", "Olga", "Pablo", "Rebekah", "Sebastien", "Tanya", "Van", "Wendy", "Arthur", "Bertha", "Cristobal", "Dolly", "Edouard", "Fay", "Gonzalo", "Hanna", "Isaias", "Josephine", "Kyle", "Leah", "Marco", "Nana", "Omar", "Paulette", "Rene", "Sally", "Teddy", "Vicky", "Wilfred", "Ana", "Bill", "Claudette", "Danny", "Elsa", "Fred", "Grace", "Henri", "Imani", "Julian", "Kate", "Larry", "Mindy", "Nicholas", "Odette", "Peter", "Rose", "Sam", "Teresa", "Victor", "Wanda");
		add("Agatha", "Blas", "Celia", "Darby", "Estelle", "Frank", "Georgette", "Howard", "Ivette", "Javier", "Kay", "Lester", "Madeline", "Newton", "Orlene", "Paine", "Roslyn", "Seymour", "Tina", "Virgil", "Winifred", "Xavier", "Yolanda", "Zeke", "Adrian", "Beatriz", "Calvin", "Dora", "Eugene", "Fernanda", "Greg", "Hilary", "Irwin", "Jova", "Kenneth", "Lidia", "Max", "Norma", "Otis", "Pilar", "Ramon", "Selma", "Todd", "Veronica", "Wiley", "Xina", "York", "Zelda", "Aletta", "Bud", "Carlotta", "Daniel", "Emilia", "Fabio", "Gilma", "Hector", "Ileana", "John", "Kristy", "Lane", "Miriam", "Norman", "Olivia", "Paul", "Rosa", "Sergio", "Tara", "Vicente", "Willa", "Xavier", "Yolanda", "Zeke", "Alvin", "Barbara", "Cosme", "Dalila", "Erick", "Flossie", "Gil", "Henriette", "Ivo", "Juliette", "Kiko", "Lorena", "Mario", "Narda", "Octave", "Priscilla", "Raymond", "Sonia", "Tico", "Velma", "Wallis", "Xina", "York", "Zelda", "Amanda", "Boris", "Cristina", "Douglas", "Elida", "Fausto", "Genevieve", "Hernan", "Iselle", "Julio", "Karina", "Lowell", "Marie", "Norbert", "Odalys", "Polo", "Rachel", "Simon", "Trudy", "Vance", "Winnie", "Xavier", "Yolanda", "Zeke", "Andres", "Blanca", "Carlos", "Dolores", "Enrique", "Felicia", "Guillermo", "Hilda", "Ignacio", "Jimena", "Kevin", "Linda", "Marty", "Nora", "Olaf", "Pamela", "Rick", "Sandra", "Terry", "Vivian", "Waldo", "Xina", "York", "Zelda");
		add("Akoni", "Ema", "Hone", "Iona", "Keli", "Lala", "Moke", "Nolo", "Olana", "Pena", "Ulana", "Wale", "Aka", "Ekeka", "Hene", "Iolana", "Keoni", "Lino", "Mele", "Nona", "Oliwa", "Pama", "Upana", "Wene", "Alika", "Ele", "Huko", "Iopa", "Kika", "Lana", "Maka", "Neki", "Omeka", "Pewa", "Unala", "Wali", "Ana", "Ela", "Halola", "Iune", "Kilo", "Loke", "Malia", "Niala", "Oho", "Pali", "Ulika", "Walaka");
	}
	
	public static void add(String... names)
	{
		for(String name : names)
			if (!NAMES.containsKey(name))
				NAMES.put(name, Boolean.FALSE);
	}
	
	public static String get()
	{
		if (used >= NAMES.size())
			init();
		
		String name;
		int tries = 0;
		
		while (tries < NAMES.size())
		{
			name = tries == 0 ? NAMES.randomKey() : NAMES.nextKey();
			
			if (!NAMES.getCurrent())
			{
				NAMES.put(name, Boolean.TRUE);
				return name;
			}
				
			tries++;
		}
		
		return "";
	}
}
