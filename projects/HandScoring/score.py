def rank_hand(hand):
    values = [card[0] for card in hand]
    suits = [card[1] for card in hand]

    value_counts = {value: values.count(value) for value in values}
    sorted_value_counts = sorted(value_counts.items(), key=lambda x: (x[1], x[0]), reverse=True)
    sorted_counts = [item[1] for item in sorted_value_counts]
    sorted_values = [item[0] for item in sorted_value_counts]

    is_flush = len(set(suits)) == 1
    is_straight = len(set(sorted_values)) == 5 and max(sorted_values) - min(sorted_values) == 4

    if is_straight and is_flush:
        return (8, sorted_values)
    if sorted_counts == [4, 1]:
        return (7, sorted_values)
    if sorted_counts == [3, 2]:
        return (6, sorted_values)
    if is_flush:
        return (5, sorted_values)
    if is_straight:
        return (4, sorted_values)
    if sorted_counts == [3, 1, 1]:
        return (3, sorted_values)
    if sorted_counts == [2, 2, 1]:
        return (2, sorted_values)
    if sorted_counts == [2, 1, 1, 1]:
        return (1, sorted_values)
    return (0, sorted_values)


def score_poker_hand(hand):
    value_map = {'2': 2, '3': 3, '4': 4, '5': 5, '6': 6, '7': 7, '8': 8, '9': 9, 'T': 10, 'J': 11, 'Q': 12, 'K': 13,
                 'A': 14}
    hand = [(value_map[card[0]], card[1]) for card in hand]
    rank, sorted_values = rank_hand(hand)
    return (rank, sorted_values)


# Example usage:
hand = ['2H', '3H', '4H', '5H', '6H']
print(score_poker_hand(hand))


