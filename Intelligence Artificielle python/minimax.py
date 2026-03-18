from moteur import get_legal_moves, get_shortest_path_length

def simuler_coup(state, coup, is_ia):
    undo_pos = state.ia_pos if is_ia else state.joueur_pos
    if coup[0] == "MOVE":
        if is_ia: state.ia_pos = (coup[1], coup[2])
        else: state.joueur_pos = (coup[1], coup[2])
    elif coup[0] == "WALL":
        if is_ia: state.ia_walls -= 1
        else: state.joueur_walls -= 1
        if coup[1] == "H": state.horizontal_walls.add((coup[2], coup[3]))
        else: state.vertical_walls.add((coup[2], coup[3]))
    return undo_pos

def annuler_coup(state, coup, undo_pos, is_ia):
    if coup[0] == "MOVE":
        if is_ia: state.ia_pos = undo_pos
        else: state.joueur_pos = undo_pos
    elif coup[0] == "WALL":
        if is_ia: state.ia_walls += 1
        else: state.joueur_walls += 1
        if coup[1] == "H": state.horizontal_walls.remove((coup[2], coup[3]))
        else: state.vertical_walls.remove((coup[2], coup[3]))

def evaluate_state(state):
    ia_dist = get_shortest_path_length(state, state.ia_pos, 8)
    joueur_dist = get_shortest_path_length(state, state.joueur_pos, 0)
    
    if ia_dist == 0: return 1000
    if joueur_dist == 0: return -1000
    
    score = joueur_dist - ia_dist
    score += (state.ia_walls - state.joueur_walls) * 0.5
    return score

def minimax(state, depth, alpha, beta, is_maximizing_player):
    if depth == 0 or state.ia_pos[0] == 8 or state.joueur_pos[0] == 0:
        return evaluate_state(state), None

    meilleur_coup = None

    if is_maximizing_player:
        max_eval = float('-inf')
        for coup in get_legal_moves(state, is_ia=True):
            undo_info = simuler_coup(state, coup, is_ia=True)
            eval_score, _ = minimax(state, depth - 1, alpha, beta, False)
            annuler_coup(state, coup, undo_info, is_ia=True)
            
            if eval_score > max_eval:
                max_eval = eval_score
                meilleur_coup = coup
            
            alpha = max(alpha, eval_score)
            if beta <= alpha:
                break
        return max_eval, meilleur_coup

    else:
        min_eval = float('inf')
        for coup in get_legal_moves(state, is_ia=False):
            undo_info = simuler_coup(state, coup, is_ia=False)
            eval_score, _ = minimax(state, depth - 1, alpha, beta, True)
            annuler_coup(state, coup, undo_info, is_ia=False)
            
            if eval_score < min_eval:
                min_eval = eval_score
                meilleur_coup = coup
                
            beta = min(beta, eval_score)
            if beta <= alpha:
                break
        return min_eval, meilleur_coup
