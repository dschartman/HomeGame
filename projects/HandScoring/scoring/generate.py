import itertools
import struct
from .config import VALUE_MAP


def generate_poker_hand_scores():
    cards = [f'{value}{suit}' for value in '23456789TJQKA' for suit in 'SHCD']
    all_hands = itertools.combinations(cards, 5)

    hand_scores = {}
    for hand in all_hands:
        hand = [(VALUE_MAP[card[0]], card[1]) for card in hand]
        rank, sorted_values, rank_name = _rank_hand(hand)
        hand_score = _rank_to_int(rank, sorted_values)
        hand_key = hand_to_hash(hand)
        hand_scores[hand_key] = {
            "score": hand_score,
            "rank": rank_name
        }

    return hand_scores


def _rank_hand(hand):
    values = [card[0] for card in hand]
    suits = [card[1] for card in hand]

    value_counts = {value: values.count(value) for value in values}
    sorted_value_counts = sorted(value_counts.items(), key=lambda x: (x[1], x[0]), reverse=True)
    sorted_counts = [item[1] for item in sorted_value_counts]
    sorted_values = [item[0] for item in sorted_value_counts]

    is_flush = len(set(suits)) == 1
    is_straight = len(set(sorted_values)) == 5 and max(sorted_values) - min(sorted_values) == 4

    if is_straight and is_flush:
        if sorted_values[0] == 14:
            return 9, sorted_values, "Royal Flush"
        else:
            return 8, sorted_values, "Straight Flush"
    if sorted_counts == [4, 1]:
        return 7, sorted_values, "Quads"
    if sorted_counts == [3, 2]:
        return 6, sorted_values, "Full House"
    if is_flush:
        return 5, sorted_values, "Flush"
    if is_straight:
        return 4, sorted_values, "Straight"
    if sorted_counts == [3, 1, 1]:
        return 3, sorted_values, "Trips"
    if sorted_counts == [2, 2, 1]:
        return 2, sorted_values, "Two Pair"
    if sorted_counts == [2, 1, 1, 1]:
        return 1, sorted_values, "Pair"
    return 0, sorted_values, "High"


def score_hand(hand):
    hand = [(VALUE_MAP[card[0]], card[1]) for card in hand]
    rank, sorted_values, rank_name = _rank_hand(hand)
    score = _rank_to_int(rank, sorted_values)

    return score


def hand_to_hash(hand):
    hand_value = 0
    for card in hand:
        hand_value |= (1 << (VALUE_MAP[card[0]] + 4 * ("SHCD".index(card[1]))))
    return hand_value


def hash_to_hand(hand_hash):
    suits = 'SHCD'
    values = '23456789TJQKA'

    hand = []
    for suit_idx, suit in enumerate(suits):
        for value_idx, value in enumerate(values):
            card_bit = VALUE_MAP[value] + 4 * suit_idx
            if hand_hash & (1 << card_bit):
                hand.append(f'{value}{suit}')

    return hand


def _rank_to_int(rank, sorted_values):
    score = rank << 20
    for i, value in enumerate(sorted_values):
        score |= value << (16 - 4 * i)

    return score


def save_hand_scores_to_file(hand_scores, filename):
    with open(filename, 'wb') as f:
        for hand_hash, score in hand_scores.items():
            f.write(struct.pack('I I', hand_hash, score))


def load_hand_scores_from_file(filename):
    hand_scores = {}
    with open(filename, 'rb') as f:
        while True:
            data = f.read(8)
            if not data:
                break
            hand_hash, score = struct.unpack('I I', data)
            hand_scores[hand_hash] = score
    return hand_scores
