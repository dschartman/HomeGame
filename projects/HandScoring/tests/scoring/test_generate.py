import pytest

from scoring import generate
from scoring.config import *


@pytest.mark.parametrize("expected_rank,cards", [
    (0, ["2H", "3H", "4H", "5H", "7D"]),
    (1, ["2H", "2D", "4H", "5H", "7D"]),
    (2, ["2H", "2D", "4H", "4D", "7D"]),
    (3, ["2H", "2D", "2S", "4D", "7D"]),
    (4, ["2H", "3D", "4S", "5D", "6D"]),
    (5, ["2H", "3H", "4H", "5H", "7H"]),
    (6, ["2H", "2D", "2S", "4D", "4H"]),
    (7, ["2H", "2D", "2S", "2C", "7D"]),
    (8, ["2H", "3H", "4H", "5H", "6H"]),
    (9, ["AH", "KH", "QH", "JH", "TH"]),
])
def test_score_poker_hand(expected_rank, cards):
    hand = [(VALUE_MAP[card[0]], card[1]) for card in cards]
    actual_rank, card_values, rank_name = generate._rank_hand(hand)

    assert expected_rank == actual_rank


@pytest.mark.parametrize("expected_rank,cards", [
    (0, ["2H", "3H", "4H", "5H", "7D"]),
    (1, ["2H", "2D", "4H", "5H", "7D"]),
    (2, ["2H", "2D", "4H", "4D", "7D"]),
    (3, ["2H", "2D", "2S", "4D", "7D"]),
    (4, ["2H", "3D", "4S", "5D", "6D"]),
    (5, ["2H", "3H", "4H", "5H", "7H"]),
    (6, ["2H", "2D", "2S", "4D", "4H"]),
    (7, ["2H", "2D", "2S", "2C", "7D"]),
    (8, ["2H", "3H", "4H", "5H", "6H"]),
])
def test_score_to_int(expected_rank, cards):
    score = generate.score_hand(cards)
    print(score)


@pytest.mark.parametrize("winning_hand,losing_hand", [
    (["2H", "3H", "4H", "5H", "8D"], ["2C", "3S", "4D", "5D", "7D"]),
    (["AH", "KH", "QH", "JH", "TH"], ["2C", "3S", "4D", "5D", "7D"]),
    (["AH", "KH", "QH", "JH", "TH"], ["2C", "2H", "2D", "7D", "7H"]),
])
def test_compare_two_hands(winning_hand, losing_hand):
    winning_score = generate.score_hand(winning_hand)
    losing_score = generate.score_hand(losing_hand)

    assert winning_score > losing_score


def test_hand_to_hash():
    hand = ["AH", "KH", "QH", "JH", "TH"]
    print(generate.hand_to_hash(hand))


def test_generate_poker_hand_scores():
    generate.generate_poker_hand_scores()
