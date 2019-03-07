import sys
import argparse
import os
import re
import socket
import thread

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


def handle_communication(input, output):
  while True:
    sentence = input.readline().rstrip('\n')
    if not sentence:
      break
  
    if sentence == "textsend":
      output.write("computationdone\n")
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
    for i, p in enumerate(pid_pred[0]):
      if pid_data.label_dict.idx2str[p] == 'V':
        predicates.append(i)
        feats = [1 if j == i else 0 for j in range(num_tokens)]
        s1.append((s1_sent, feats, l0))

    if len(s1) == 0:
      output.write("empty\n")
      continue

    x, _, _, weights = srl_data.get_test_data(s1, batch_size=None)
    srl_pred, scores = srl_pred_function(x, weights)

    arguments = []
    for i, sc in enumerate(scores):
      viterbi_pred, _ = viterbi_decode(sc, transition_params)
      arg_spans = bio_to_spans(viterbi_pred, srl_data.label_dict)
      arguments.append(arg_spans)

    output.write(str(arguments) + "\n")


def handle_connection(connection):
  f = connection.makefile('rw', 0)
  try:
    handle_communication(f, f)
  finally:
    f.close()
    connection.shutdown(socket.SHUT_RDWR)
    connection.close()


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
  
  parser.add_argument('--server',
                      action="store_true",
                      required=False,
                      help='Startup TCP Server instead of Stdin/Stdout')
  
  parser.add_argument('--host',
                      type=str,
                      default='',
                      required=False,
                      help='Address to bind TCP Server, none means all available')
  
  parser.add_argument('--port',
                      type=int,
                      default=6756,
                      required=False,
                      help='Port to bind TCP Server')

  args = parser.parse_args()
  
  try:
    pid_model, pid_data = load_model(args.pidmodel, 'propid')
    srl_model, srl_data = load_model(args.model, 'srl')
    transition_params = get_transition_params(srl_data.label_dict.idx2str)
    
    pid_pred_function = pid_model.get_distribution_function()
    srl_pred_function = srl_model.get_distribution_function()
  except:
    sys.stderr.write("failed to startup deep_srl\n")
    sys.exit(-1)

  if args.server:
    server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server_socket.bind((args.host, args.port))
    server_socket.listen(1)
    while True:
      connection, address = server_socket.accept()
      thread.start_new_thread(handle_connection, (connection,))
  else:
    print "initsuccessful"
    try:
      handle_communication(sys.stdin, sys.stdout)
    except EOFError:
      sys.exit(0)
  
  
