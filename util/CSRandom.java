package clashsoft.clashsoftapi.util;

import java.util.Random;

public class CSRandom extends Random
{
	public static int nextInt(Random par1Random, int par2, int par3)
	{
		return par2 + par1Random.nextInt(par3 - par2);
	}
	
	public static String getNextRandomName()
	{
		return "TODO";
	}
}