FROM debian:stretch-backports

RUN apt-get update

RUN apt-get install -y python python-pip
RUN pip install numpy theano protobuf nltk==3.4.1

RUN apt-get install -y git git-lfs wget unzip
RUN git clone --depth 1 https://github.com/luheng/deep_srl.git
WORKDIR "/deep_srl/resources"
RUN ls *.tar.gz | xargs -n1 tar -zxvf
WORKDIR "/deep_srl"
RUN bash -c "scripts/fetch_required_data.sh &> /dev/null"
RUN wget https://raw.githubusercontent.com/tub-aset/gateplugin-DeepSRLAdapter/master/src/main/python/gate_deepSRL.py -O python/gate_deepSRL.py

RUN sed -i -e 's/, use_cuda=True//g' python/neural_srl/theano/layer.py

CMD ["python", "python/gate_deepSRL.py", "--model", "resources/conll05_model/", "--server"]
EXPOSE 6756