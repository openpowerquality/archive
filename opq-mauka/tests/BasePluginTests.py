import unittest
import plugins.base

class BasePluginTeests(unittest.TestCase):

    def setUp(self):
        self.mauka_plugin = plugins.base.MaukaPlugin()
        pass

    def tearDown(self):
        pass

    def test_run_plugin(self):
        pass

    def test_protobuf_decode_measurement(self):
        pass

    def test_json_encoder(self):
        pass

    def test_to_json(self):
        pass

    def test_from_json(self):
        pass

    def test_get_mongo_client(self):
        pass

    def test_config_get(self):
        pass

    def test_on_message(self):
        pass

    def test_produce(self):
        pass

    def test_run(self):
        pass