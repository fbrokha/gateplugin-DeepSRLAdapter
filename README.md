# gateplugin-DeepSRLAdapter
Integrate DeepSRL into GATE

This is a java wrapper to execute the [DeepSRL](https://github.com/luheng/deep_srl) approach, parse the STDOUT and add annotations to GATE as a Processing Resource.

It does not bundle DeepSRL, as it has its own [license](https://github.com/luheng/deep_srl/blob/master/LICENSE).


## Setup

* Follow the instructions on [DeepSRL](https://github.com/luheng/deep_srl) to fulfill the listed prerequisites. (The nltk and tcsh prerequisites are not needed)

* Extract the models `conll05_model.tar.gz` and `conll05_propid_model.tar.gz`

* Place the packaged script `gate_deepSRL.py` (see `src\main\python\`) into the DeepSRL folder `deep_srl-master\python`

## Parameters for DeepSRLAdapter

DeepSRLAdapter starts an own python process and handles communication via Stdin/Stdout.

These are the parameters which have to be set when instantiating the DeepSRLAdapter as a processing resource in GATE.

* `deepSRLExecutable`: Path to the `gate_deepSRL.py`-script (inside the `deep_srl-master\python`-folder, see above)

* `modelPath`: Path to the extracted `conll05_model`-folder

* `propidModelPath`: Path to the extracted `conll05_propid_model`-folder 

* `pythonExecutable`: Path to the Python2 executable

* `printOutput`: Print Stdout while script startup

* `printError`: Print Stderr while script running

## Parameters for DeepSRLClient

DeepSRLClient communicates with an server via TCP/IP. You have to start the `gate_deepSRL.py`-script yourself with the `--server` parameter and optionally `--host hostname` (default: all available network interfaces) and `--port port` (default: 6756). The communication is not encrypted.

These are the parameters which have to be set when instantiating the DeepSRLClient as a processing resource in GATE.

* `host`: Hostname of the server (default: localhost)

* `port`: Port of the server (default: 6756)