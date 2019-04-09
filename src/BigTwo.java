import java.util.ArrayList;

/**
 * This class is used to model a Big Two card game. It stores the a deck of cards, 
 * players, hands of cards on table, the index of current player and a console as
 * an interface for players. 
 * 
 * @author davidliu
 *
 */
public class BigTwo implements CardGame
{
	/**
	 * Constructor for the BigTwo class
	 */
	public BigTwo()
	{
		playerList = new ArrayList<CardGamePlayer>();
		handsOnTable = new ArrayList<Hand>();

		for(int i=0;i<4;i++)
		{
			CardGamePlayer gamePlayer = new CardGamePlayer("Player"+i);
			playerList.add(gamePlayer);
		}
		bigTwoTable = new BigTwoTable(this);
	}
	
	private Deck deck; //a deck of cards
	private ArrayList<CardGamePlayer> playerList; //a list of player
	private ArrayList<Hand> handsOnTable; //a list of hands
	private BigTwoTable bigTwoTable; //a BigTwoConsole object for providing the user interface
	
	/**
	 * A method for retrieving the deck of cards being used
	 * 
	 * @return
	 * 		the deck used in current game
	 */
	public Deck getDeck()
	{
		return this.deck;
	}
	
	/**
	 * A method for retrieving the list of players
	 * 
	 * @return
	 * 		the array list for players in current game
	 */
	public ArrayList<CardGamePlayer> getPlayerList()
	{
		return this.playerList;
	}
	
	/**
	 * A method for retrieving the list of hands played on the table
	 * 
	 * @return
	 * 		The array list of hands on table in current game
	 */
	public ArrayList<Hand> getHandsOnTable()
	{
		return this.handsOnTable;
	}
	
	/**
	 * A method for retrieving the index of the current player
	 * 
	 * @return
	 * 		the index of the current player
	 */
	public int getCurrentIdx()
	{
		return bigTwoTable.getActivePlayer();
	}

	@Override
	/**
	 * A method for starting the game with a (shuffled) deck of
	 * cards supplied as the argument. It implements the Big Two
	 * game logics
	 *  
	 * @param deck
	 * 			The shuffled deck of cards
	 */
	public void start(Deck deck)
	{
		for(int i=0;i<playerList.size();i++)
		{
			playerList.get(i).removeAllCards();
		}
		int currentIdx = -1; // starting player
		this.deck = deck;
		handsOnTable.clear();
		//then distribute the cards
		for(int i=0;i<13;i++)
		{
			for(int j=0;j<playerList.size();j++)
			{
				Card card = deck.removeCard(0);
				playerList.get(j).addCard(card);
				if(card.getRank()==2 && card.getSuit()==0)
					currentIdx = j; //the one with Diamond 3 is the first player
			}
		}
		for(int i=0;i<playerList.size();i++)
		{
			playerList.get(i).sortCardsInHand();
		}
		//TODO: starting animation
		//Interaction begin
		bigTwoTable.setActivePlayer(currentIdx);
		bigTwoTable.repaint();
	}
	
	/**
	 * The main function. It should create a Big Two card game, 
	 * create and shuffle a deck of cards, and start the game with
	 * the deck of cards
	 */
	public static void main(String[] args)
	{
		BigTwo game = new BigTwo();
		BigTwoDeck deck = new BigTwoDeck();
		deck.shuffle();
		game.start(deck);
	}
	
	/**
	 * A method for returning a valid hand from the specified list
	 * of cards of the player
	 * 
	 * @param player
	 * 			The current player who is going to play the cards
	 * @param cards
	 * 			The cards from which a valid hand is going to be
	 * 			returned from
	 * @return
	 * 		The valid hands from the given cards; if no valid hand,
	 * 		return null
	 */
	public static Hand composeHand(CardGamePlayer player, CardList cards)
	{
		//check whether the cards is valid or not
		Hand hand = null;
		if(cards.size()==1)			hand = new Single(player,cards);
		else if (cards.size()==2)	hand = new Pair(player,cards);
		else if (cards.size()==3)	hand = new Triple(player, cards);
		else //suppose to have 5 cards
		{
			if(cards.size()!=5)		return null;
			//otherwise it can be flush, fullhouse, Quad, Straight or StraightFlush
			Flush flushHand = new Flush(player, cards);
			if(flushHand.isValid())
			{
				hand = flushHand;
			}
			else
			{
				FullHouse fullHouseHand = new FullHouse(player, cards);
				if(fullHouseHand.isValid())
				{
					hand = fullHouseHand;
				}
				else
				{
					Quad quadHand = new Quad(player, cards);
					if(quadHand.isValid())
					{
						hand = quadHand;
					}
					else
					{
						Straight straigthHand = new Straight(player, cards);
						if(straigthHand.isValid())
						{
							hand = straigthHand;
						}
						else
						{
							hand = new StraightFlush(player, cards);	
						}
					}
				}
			}

		}

		if(hand==null || !hand.isValid())	return null;
		return hand;
	}

	@Override
	public int getNumOfPlayers()
	{
		return this.playerList.size();
	}

	@Override
	public void makeMove(int playerID, int[] cardIdx)
	{
		checkMove(playerID, cardIdx);
		
	}

	@Override
	public void checkMove(int playerID, int[] cardIdx)
	{
		CardGamePlayer player = playerList.get(playerID);
		CardList cardInHand = player.play(cardIdx);
		boolean legalMove = false;
		if(cardInHand != null) // not a pass
		{
			Hand hand = composeHand(player, cardInHand);
			if(hand != null) //not invalid
			{
				if(handsOnTable.size()==0 && hand.contains(new BigTwoCard(0, 2)))//first player
				{
					legalMove = true;
				}
				if(hand.beats(handsOnTable.get(handsOnTable.size()-1)) || 
				   handsOnTable.get(handsOnTable.size()-1).getPlayer() == player)
				//win or pass to the same person
				{
					legalMove = true;	
				}
			}//end of legal checking
			if(legalMove)
			{
				handsOnTable.add(hand);
				player.removeCards(cardInHand);
				bigTwoTable.printMsg("{"+hand.getType()+"} ");
				for(int i=0;i<hand.size()-1;i++)
				{
					bigTwoTable.printMsg("["+hand.getCard(i).toString()+"] ");
				}
				bigTwoTable.printMsg("["+hand.getCard(hand.size()-1).toString()+"]\n");
			}
			else
			{
				bigTwoTable.printMsg("Not a legal move");
			}//end of a non-pass moving
		}
		else //pass
		{
			bigTwoTable.printMsg("{Pass}");
		}
		bigTwoTable.setActivePlayer((playerID+1)%getNumOfPlayers());
		//bigTwoTable.repaint() ---- no need, already included in setActiveplayer
		if(endOfGame())
		{
			bigTwoTable.printMsg("Game ends");
			for(int i=0;i<playerList.size();i++)
			{
				bigTwoTable.printMsg("Player "+i+" ");
				if(playerList.get(i).getCardsInHand().isEmpty())
					bigTwoTable.printMsg("wins the game.\n");
				else
					bigTwoTable.printMsg("has "+playerList.get(i).getCardsInHand().size()+" cards in hand.\n");
			}//END: final print
			bigTwoTable.disable();
			bigTwoTable.discloseAllPlayers();
			bigTwoTable.repaint();
		}
	}

	@Override
	public boolean endOfGame()
	{
		//check for the end
		for(CardGamePlayer onePlayer : playerList)
		{
			if(onePlayer.getCardsInHand().isEmpty())
			{
				return true;
			}
		}
		return false;
	}
}
