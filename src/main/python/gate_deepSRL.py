import sys
import argparse
import os
import re

import numpy
import theano

from neural_srl.shared import *
from neural_srl.shared.constants import *
from neural_srl.shared.dictionary import Dictionary
from neural_srl.shared.inference import *
from neural_srl.shared.tagger_data import TaggerData
from neural_srl.shared.measurements import Timer
from neural_srl.shared.evaluation import SRLEvaluator
from neural_srl.shared.io_utils import bio_to_spans
from neural_srl.shared.reader import string_sequence_to_ids
from neural_srl.shared.scores_pb2 import *
from neural_srl.shared.tensor_pb2 import *
from neural_srl.theano.tagger import BiLSTMTaggerModel
from neural_srl.theano.util import floatX

from interactive import load_model

def read_sentence():
  try:
    return raw_input()
  except EOFError:
    sys.exit(0)

if __name__ == "__main__":

  parser = argparse.ArgumentParser(description=__doc__)
  parser.add_argument('--model',
                      type=str,
                      default='',
                      required=True,
                      help='SRL Model path.')

  parser.add_argument('--pidmodel',
                      type=str,
                      default='',
                      help='Predicate identfication model path.')

  args = parser.parse_args()
  
  try:
    pid_model, pid_data = load_model(args.pidmodel, 'propid')
    srl_model, srl_data = load_model(args.model, 'srl')
    transition_params = get_transition_params(srl_data.label_dict.idx2str)
    
    pid_pred_function = pid_model.get_distribution_function()
    srl_pred_function = srl_model.get_distribution_function()
  except:
    sys.exit(-1)

  print "initsuccessful"
  
  sentence = read_sentence()
    
  while sentence:

    if sentence == "textsend":
      print "computationdone"
      sentence = read_sentence()
      continue

    tokenized_sent = sentence.split()

    num_tokens = len(tokenized_sent)
    s0 = string_sequence_to_ids(tokenized_sent, pid_data.word_dict, True)
    l0 = [0 for _ in s0]
    x, _, _, weights = pid_data.get_test_data([(s0, l0)], batch_size=None)
    pid_pred, scores0 = pid_pred_function(x, weights)

    s1_sent = string_sequence_to_ids(tokenized_sent, srl_data.word_dict, True)
    s1 = []
    predicates = []
    for i,p in enumerate(pid_pred[0]):
      if pid_data.label_dict.idx2str[p] == 'V':
        predicates.append(i)
        feats = [1 if j == i else 0 for j in range(num_tokens)]
        s1.append((s1_sent, feats, l0))

    if len(s1) == 0:
      print "empty"
      sentence = read_sentence()
      continue

    x, _, _, weights = srl_data.get_test_data(s1, batch_size=None)
    srl_pred, scores = srl_pred_function(x, weights)

    arguments = []
    for i, sc in enumerate(scores):
      viterbi_pred, _ = viterbi_decode(sc, transition_params)
      arg_spans = bio_to_spans(viterbi_pred, srl_data.label_dict)
      arguments.append(arg_spans)

    print arguments
    sentence = read_sentence()